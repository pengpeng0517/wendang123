package com.wms.modules.location.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.modules.location.dto.LocationPolicyEvaluationResponse;
import com.wms.modules.location.dto.LocationPolicyStatusResponse;
import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.dto.LocationRecommendationResponse;
import com.wms.modules.location.dto.PolicyDecision;
import com.wms.modules.location.entity.LocationAllocationLog;
import com.wms.modules.location.entity.LocationPolicyEvent;
import com.wms.modules.location.entity.WarehouseLocation;
import com.wms.modules.location.enums.PolicyMode;
import com.wms.modules.location.enums.PolicyType;
import com.wms.modules.location.mapper.LocationAllocationLogMapper;
import com.wms.modules.location.policy.LocationPolicyContext;
import com.wms.modules.location.policy.OnnxPolicyEngine;
import com.wms.modules.location.policy.RewardCalculator;
import com.wms.modules.location.policy.RulePolicyEngine;
import com.wms.modules.location.service.LocationAllocationService;
import com.wms.modules.location.service.LocationPolicyConfigService;
import com.wms.modules.location.service.LocationPolicyEventService;
import com.wms.modules.location.service.WarehouseLocationService;
import com.wms.modules.material.entity.Material;
import com.wms.modules.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LocationAllocationServiceImpl implements LocationAllocationService {

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Autowired
    private LocationAllocationLogMapper locationAllocationLogMapper;

    @Autowired
    private LocationPolicyEventService locationPolicyEventService;

    @Autowired
    private LocationPolicyConfigService locationPolicyConfigService;

    @Autowired
    private RulePolicyEngine rulePolicyEngine;

    @Autowired
    private OnnxPolicyEngine onnxPolicyEngine;

    @Autowired
    private RewardCalculator rewardCalculator;

    @Autowired
    private MaterialService materialService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LocationRecommendationResponse recommend(LocationRecommendationRequest request) {
        long start = System.currentTimeMillis();

        LocationRecommendationRequest normalizedRequest = request == null ? new LocationRecommendationRequest() : request;
        LocationPolicyContext context = buildContext(normalizedRequest);

        List<WarehouseLocation> activeLocations = warehouseLocationService.listLocations(null, 1);
        if (activeLocations == null || activeLocations.isEmpty()) {
            return null;
        }
        context.setActiveLocations(activeLocations);

        PolicyMode mode = locationPolicyConfigService.getPolicyMode();

        PolicyDecision ruleDecision = rulePolicyEngine.decide(context);
        if (ruleDecision == null) {
            return null;
        }

        PolicyDecision rlDecision = null;
        if (mode != PolicyMode.RULE && shouldRunShadowSample(mode)) {
            ensureOnnxLoaded();
            rlDecision = onnxPolicyEngine.decide(context);
        }

        double threshold = locationPolicyConfigService.getConfidenceThreshold();

        PolicyDecision finalDecision = ruleDecision;
        String fallbackReason = null;

        if (mode == PolicyMode.SHADOW) {
            finalDecision = ruleDecision;
            if (rlDecision == null) {
                fallbackReason = "shadow_rl_unavailable";
            }
        } else if (mode == PolicyMode.MANUAL) {
            if (rlDecision != null && isDecisionLegal(rlDecision, context)) {
                finalDecision = rlDecision;
            } else {
                finalDecision = ruleDecision;
                fallbackReason = rlDecision == null ? "manual_rl_unavailable" : "manual_rl_illegal";
            }
        } else if (mode == PolicyMode.AUTO) {
            if (rlDecision == null) {
                fallbackReason = "auto_rl_unavailable";
            } else if (!isDecisionLegal(rlDecision, context)) {
                fallbackReason = "auto_rl_illegal";
            } else if (rlDecision.getConfidence() == null || rlDecision.getConfidence() < threshold) {
                fallbackReason = "auto_rl_low_confidence";
            } else {
                finalDecision = rlDecision;
            }

            if (fallbackReason != null) {
                finalDecision = ruleDecision;
            }
        }

        if (fallbackReason != null) {
            finalDecision.setFallbackReason(fallbackReason);
        }

        long latency = System.currentTimeMillis() - start;

        LocationRecommendationResponse response = toResponse(finalDecision, mode, context.getTraceId(), fallbackReason);
        WarehouseLocation selectedLocation = findLocationByCode(response.getLocationCode(), activeLocations);
        RewardCalculator.RewardResult rewardResult = rewardCalculator.calculate(context, selectedLocation);

        recordPolicyEvent(context,
                mode,
                ruleDecision,
                rlDecision,
                finalDecision,
                response,
                rewardResult,
                latency,
                normalizedRequest);

        return response;
    }

    @Override
    public void recordAllocation(Long materialId,
                                 String materialCode,
                                 String materialName,
                                 Integer inboundQuantity,
                                 String operator,
                                 LocationRecommendationRequest request,
                                 LocationRecommendationResponse response) {
        if (response == null || isBlank(response.getLocationCode())) {
            return;
        }

        LocationAllocationLog log = new LocationAllocationLog();
        log.setMaterialId(materialId);
        log.setMaterialCode(materialCode);
        log.setMaterialName(materialName);
        log.setInboundQuantity(normalizeQuantity(inboundQuantity));
        log.setLocationCode(response.getLocationCode());
        log.setScore(response.getScore());
        log.setReason(response.getReason());
        log.setSeasonTag(request == null ? null : request.getSeasonTag());
        log.setPolicyType(response.getPolicyType());
        log.setPolicyVersion(response.getPolicyVersion());
        log.setConfidence(response.getConfidence());
        log.setFallbackReason(response.getFallbackReason());
        log.setTraceId(response.getTraceId());
        log.setOperator(operator);
        log.setCreateTime(LocalDateTime.now());

        locationAllocationLogMapper.insert(log);
        completePolicyEvent(response.getTraceId(), response.getLocationCode(), operator);
    }

    @Override
    public String getCurrentPolicyMode() {
        return locationPolicyConfigService.getPolicyMode().getValue();
    }

    @Override
    public LocationPolicyStatusResponse getPolicyStatus() {
        LocationPolicyStatusResponse status = new LocationPolicyStatusResponse();
        status.setMode(locationPolicyConfigService.getPolicyMode().getValue());
        status.setModelVersion(locationPolicyConfigService.getModelVersion());
        status.setModelPath(locationPolicyConfigService.getModelPath());
        status.setConfidenceThreshold(locationPolicyConfigService.getConfidenceThreshold());
        status.setShadowSampleRate(locationPolicyConfigService.getShadowSampleRate());

        OnnxPolicyEngine.OnnxModelStatus modelStatus = onnxPolicyEngine.getStatus();
        status.setModelLoaded(modelStatus.isLoaded());
        status.setModelLoadMessage(modelStatus.getMessage());
        status.setModelLastLoadTime(modelStatus.getLastLoadTime());

        return status;
    }

    @Override
    public boolean reloadPolicyModel(String modelPath, String modelVersion, String operator) {
        String finalPath = isBlank(modelPath) ? locationPolicyConfigService.getModelPath() : modelPath.trim();
        String finalVersion = isBlank(modelVersion) ? locationPolicyConfigService.getModelVersion() : modelVersion.trim();

        boolean success = onnxPolicyEngine.reload(finalPath, finalVersion);
        if (success) {
            locationPolicyConfigService.updateModelConfig(finalPath, finalVersion, operator);
        }
        return success;
    }

    @Override
    public void updatePolicyMode(String mode, String operator) {
        PolicyMode policyMode = PolicyMode.fromValue(mode);
        locationPolicyConfigService.updatePolicyMode(policyMode, operator);
    }

    @Override
    public LocationPolicyEvaluationResponse evaluatePolicy(LocalDateTime from, LocalDateTime to) {
        LocationPolicyEvaluationResponse response = new LocationPolicyEvaluationResponse();
        LocalDateTime finalTo = to == null ? LocalDateTime.now() : to;
        LocalDateTime finalFrom = from == null ? finalTo.minusDays(7) : from;

        response.setFrom(finalFrom);
        response.setTo(finalTo);
        response.setItems(locationPolicyEventService.evaluate(finalFrom, finalTo));
        return response;
    }

    @Override
    public void completePolicyEvent(String traceId, String executedActionCode, String operator) {
        locationPolicyEventService.completeEvent(traceId, executedActionCode, operator);
    }

    private void ensureOnnxLoaded() {
        OnnxPolicyEngine.OnnxModelStatus status = onnxPolicyEngine.getStatus();
        if (status.isLoaded()) {
            return;
        }

        String path = locationPolicyConfigService.getModelPath();
        String version = locationPolicyConfigService.getModelVersion();
        if (!isBlank(path)) {
            onnxPolicyEngine.reload(path, version);
        }
    }

    private boolean shouldRunShadowSample(PolicyMode mode) {
        if (mode != PolicyMode.SHADOW) {
            return true;
        }
        double sampleRate = locationPolicyConfigService.getShadowSampleRate();
        if (sampleRate >= 1.0) {
            return true;
        }
        if (sampleRate <= 0.0) {
            return false;
        }
        return Math.random() < sampleRate;
    }

    private boolean isDecisionLegal(PolicyDecision decision, LocationPolicyContext context) {
        if (decision == null || isBlank(decision.getLocationCode())) {
            return false;
        }

        WarehouseLocation location = findLocationByCode(decision.getLocationCode(), context.getActiveLocations());
        if (location == null) {
            return false;
        }

        if (location.getStatus() != null && location.getStatus() != 1) {
            return false;
        }

        int inbound = Math.max(1, context.getInboundQuantity());
        int available = safeInt(location.getCapacity()) - safeInt(location.getCurrentLoad());
        return available >= inbound;
    }

    private LocationRecommendationResponse toResponse(PolicyDecision decision,
                                                      PolicyMode mode,
                                                      String traceId,
                                                      String fallbackReason) {
        LocationRecommendationResponse response = new LocationRecommendationResponse();
        response.setLocationCode(decision.getLocationCode());
        response.setCapacity(decision.getCapacity());
        response.setCurrentLoad(decision.getCurrentLoad());
        response.setAvailableCapacity(decision.getAvailableCapacity());
        response.setScore(decision.getScore());
        response.setReason(decision.getReason());
        response.setCandidates(decision.getCandidates());
        response.setPolicyMode(mode.getValue());
        response.setPolicyType(decision.getPolicyType() == null ? null : decision.getPolicyType().getValue());
        response.setPolicyVersion(decision.getPolicyVersion());
        response.setConfidence(decision.getConfidence());
        response.setFallbackReason(fallbackReason == null ? decision.getFallbackReason() : fallbackReason);
        response.setTraceId(traceId);
        return response;
    }

    private void recordPolicyEvent(LocationPolicyContext context,
                                   PolicyMode mode,
                                   PolicyDecision ruleDecision,
                                   PolicyDecision rlDecision,
                                   PolicyDecision finalDecision,
                                   LocationRecommendationResponse response,
                                   RewardCalculator.RewardResult rewardResult,
                                   long latency,
                                   LocationRecommendationRequest request) {
        LocationPolicyEvent event = new LocationPolicyEvent();
        event.setEventTime(LocalDateTime.now());

        Long materialId = request.getMaterialId();
        event.setMaterialId(resolveMaterialId(materialId));
        event.setMaterialCode(resolveMaterialCode(materialId));
        event.setCategoryId(resolveCategoryId(materialId));
        event.setInboundQty(context.getInboundQuantity());
        event.setCandidateSetJson(toJson(finalDecision.getCandidates()));
        event.setSelectedActionCode(response.getLocationCode());
        event.setExecutedActionCode(null);
        event.setPolicyMode(mode.getValue());
        event.setPolicyType(response.getPolicyType());
        event.setPolicyVersion(response.getPolicyVersion());
        event.setStateVectorJson(buildStateVectorJson(context, response.getLocationCode()));
        event.setRewardTotal(rewardResult.getTotal());
        event.setRewardBreakdownJson(buildRewardBreakdown(rewardResult, ruleDecision, rlDecision, latency));
        event.setIsFallback(isFallback(mode, response.getPolicyType(), response.getFallbackReason()) ? 1 : 0);
        event.setConfidence(response.getConfidence());
        event.setOperator(null);
        event.setTraceId(context.getTraceId());

        locationPolicyEventService.saveEventAsync(event);
    }

    private String buildStateVectorJson(LocationPolicyContext context, String selectedCode) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("material_id", context.getRequest().getMaterialId());
        payload.put("inbound_qty", context.getInboundQuantity());
        payload.put("season_tag", context.getSeasonTag());
        payload.put("temperature_level", context.getTemperatureLevel());
        payload.put("preferred_location", context.getPreferredLocation());
        payload.put("selected_location", selectedCode);
        return toJson(payload);
    }

    private String buildRewardBreakdown(RewardCalculator.RewardResult rewardResult,
                                        PolicyDecision ruleDecision,
                                        PolicyDecision rlDecision,
                                        long latency) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("reward", rewardResult.getBreakdown());
        map.put("rule_location", ruleDecision == null ? null : ruleDecision.getLocationCode());
        map.put("rule_score", ruleDecision == null ? null : ruleDecision.getScore());
        map.put("rl_location", rlDecision == null ? null : rlDecision.getLocationCode());
        map.put("rl_score", rlDecision == null ? null : rlDecision.getScore());
        map.put("latency_ms", latency);
        return toJson(map);
    }

    private boolean isFallback(PolicyMode mode, String policyType, String fallbackReason) {
        if (mode == PolicyMode.RULE) {
            return false;
        }
        if (fallbackReason != null && !fallbackReason.trim().isEmpty()) {
            return true;
        }
        return PolicyType.RULE.getValue().equalsIgnoreCase(policyType);
    }

    private Long resolveMaterialId(Long materialId) {
        if (materialId == null) {
            return null;
        }
        Material material = materialService.getById(materialId);
        return material == null ? null : material.getId();
    }

    private Long resolveCategoryId(Long materialId) {
        if (materialId == null) {
            return null;
        }
        Material material = materialService.getById(materialId);
        return material == null ? null : material.getCategoryId();
    }

    private String resolveMaterialCode(Long materialId) {
        if (materialId == null) {
            return null;
        }
        Material material = materialService.getById(materialId);
        return material == null ? null : material.getCode();
    }

    private LocationPolicyContext buildContext(LocationRecommendationRequest request) {
        LocationPolicyContext context = new LocationPolicyContext();
        context.setRequest(request);
        context.setInboundQuantity(normalizeQuantity(request.getInboundQuantity()));
        context.setPreferredLocation(normalizeString(request.getPreferredLocation()));
        context.setSeasonTag(normalizeString(request.getSeasonTag()));
        context.setTemperatureLevel(normalizeString(request.getTemperatureLevel()));
        context.setTraceId(generateTraceId());
        return context;
    }

    private WarehouseLocation findLocationByCode(String code, List<WarehouseLocation> locations) {
        if (isBlank(code) || locations == null || locations.isEmpty()) {
            return null;
        }

        for (WarehouseLocation location : locations) {
            if (location != null && !isBlank(location.getCode()) && location.getCode().trim().equalsIgnoreCase(code.trim())) {
                return location;
            }
        }
        return null;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private int normalizeQuantity(Integer inboundQuantity) {
        if (inboundQuantity == null || inboundQuantity <= 0) {
            return 1;
        }
        return inboundQuantity;
    }

    private String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
