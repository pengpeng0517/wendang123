package com.wms.modules.location.policy;

import com.wms.modules.location.dto.LocationRecommendationResponse;
import com.wms.modules.location.dto.PolicyDecision;
import com.wms.modules.location.entity.WarehouseLocation;
import com.wms.modules.location.enums.PolicyType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class RulePolicyEngine implements LocationPolicyEngine {

    @Override
    public PolicyType getPolicyType() {
        return PolicyType.RULE;
    }

    @Override
    public PolicyDecision decide(LocationPolicyContext context) {
        List<WarehouseLocation> activeLocations = context.getActiveLocations();
        if (activeLocations == null || activeLocations.isEmpty()) {
            return null;
        }

        int inboundQuantity = Math.max(1, context.getInboundQuantity());
        String preferredLocation = normalizeString(context.getPreferredLocation());
        String seasonTag = normalizeString(context.getSeasonTag());
        String temperatureLevel = normalizeString(context.getTemperatureLevel());

        List<WarehouseLocation> tempMatchedLocations = activeLocations.stream()
                .filter(location -> matchTemperature(location, temperatureLevel))
                .collect(Collectors.toList());

        if (tempMatchedLocations.isEmpty()) {
            tempMatchedLocations = activeLocations;
        }

        List<WarehouseLocation> candidates = tempMatchedLocations.stream()
                .filter(location -> getAvailableCapacity(location) >= inboundQuantity)
                .collect(Collectors.toList());

        boolean insufficientCapacity = false;
        if (candidates.isEmpty()) {
            candidates = new ArrayList<WarehouseLocation>(tempMatchedLocations);
            insufficientCapacity = true;
        }

        final boolean fallbackFlag = insufficientCapacity;
        List<LocationRecommendationResponse.Candidate> scoredCandidates = candidates.stream()
                .map(location -> toCandidate(location, inboundQuantity, preferredLocation, seasonTag, temperatureLevel, fallbackFlag))
                .sorted(Comparator.comparing(LocationRecommendationResponse.Candidate::getScore).reversed())
                .collect(Collectors.toList());

        if (scoredCandidates.isEmpty()) {
            return null;
        }

        LocationRecommendationResponse.Candidate best = scoredCandidates.get(0);
        PolicyDecision decision = new PolicyDecision();
        decision.setLocationCode(best.getLocationCode());
        decision.setCapacity(best.getCapacity());
        decision.setCurrentLoad(best.getCurrentLoad());
        decision.setAvailableCapacity(best.getAvailableCapacity());
        decision.setScore(best.getScore());
        decision.setReason(best.getReason());
        decision.setConfidence(calculateConfidence(scoredCandidates));
        decision.setPolicyType(PolicyType.RULE);
        decision.setPolicyVersion("rule-v1");
        decision.setCandidates(scoredCandidates.stream().limit(3).collect(Collectors.toList()));
        return decision;
    }

    private double calculateConfidence(List<LocationRecommendationResponse.Candidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return 0.0;
        }
        if (candidates.size() == 1) {
            return 0.85;
        }

        double first = candidates.get(0).getScore() == null ? 0.0 : candidates.get(0).getScore();
        double second = candidates.get(1).getScore() == null ? 0.0 : candidates.get(1).getScore();
        double margin = Math.max(0.0, first - second);
        double confidence = 0.5 + Math.min(0.45, margin / 100.0);
        return roundScore(confidence);
    }

    private String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchTemperature(WarehouseLocation location, String requestTemperature) {
        if (requestTemperature == null) {
            return true;
        }
        String locationTemperature = normalizeString(location.getTemperatureLevel());
        if (locationTemperature == null) {
            return true;
        }
        return locationTemperature.equalsIgnoreCase(requestTemperature);
    }

    private int getAvailableCapacity(WarehouseLocation location) {
        int capacity = location.getCapacity() == null ? 0 : location.getCapacity();
        int currentLoad = location.getCurrentLoad() == null ? 0 : location.getCurrentLoad();
        return capacity - currentLoad;
    }

    private LocationRecommendationResponse.Candidate toCandidate(WarehouseLocation location,
                                                                 int inboundQuantity,
                                                                 String preferredLocation,
                                                                 String seasonTag,
                                                                 String temperatureLevel,
                                                                 boolean insufficientCapacity) {
        int capacity = location.getCapacity() == null ? 0 : location.getCapacity();
        int currentLoad = location.getCurrentLoad() == null ? 0 : location.getCurrentLoad();
        int availableCapacity = capacity - currentLoad;

        double loadRatioAfterInbound = capacity <= 0 ? 1.0 : (currentLoad + inboundQuantity) * 1.0 / capacity;
        double loadBalanceScore = Math.max(0, 45.0 - loadRatioAfterInbound * 45.0);

        int priority = location.getPriority() == null ? 0 : location.getPriority();
        double priorityScore = Math.max(0, Math.min(priority, 10)) * 4.0;

        String zone = location.getZone() == null ? "" : location.getZone().trim().toUpperCase(Locale.ROOT);
        double zoneScore;
        if (zone.startsWith("A")) {
            zoneScore = 6.0;
        } else if (zone.startsWith("B")) {
            zoneScore = 4.0;
        } else {
            zoneScore = 2.0;
        }

        double seasonScore = 0.0;
        if ("旺季".equals(seasonTag)) {
            double capacitySafety = capacity <= 0 ? 0.0 : Math.max(0.0, availableCapacity * 1.0 / capacity);
            seasonScore = capacitySafety * 20.0;
        } else if ("淡季".equals(seasonTag)) {
            seasonScore = Math.max(0.0, (1.0 - loadRatioAfterInbound) * 8.0);
        }

        double preferredScore = 0.0;
        if (preferredLocation != null && preferredLocation.equalsIgnoreCase(location.getCode())) {
            preferredScore = 20.0;
        }

        double temperatureScore = 0.0;
        String locationTemperature = normalizeString(location.getTemperatureLevel());
        if (temperatureLevel != null) {
            if (locationTemperature != null && locationTemperature.equalsIgnoreCase(temperatureLevel)) {
                temperatureScore = 8.0;
            } else if (locationTemperature == null) {
                temperatureScore = 3.0;
            }
        }

        double capacityPenalty = 0.0;
        if (availableCapacity < inboundQuantity) {
            capacityPenalty = -(inboundQuantity - availableCapacity) * 5.0;
        }

        double totalScore = loadBalanceScore + priorityScore + zoneScore + seasonScore + preferredScore + temperatureScore + capacityPenalty;

        StringBuilder reasonBuilder = new StringBuilder();
        reasonBuilder.append("载荷均衡=").append(formatScore(loadBalanceScore))
                .append(", 优先级=").append(formatScore(priorityScore))
                .append(", 分区=").append(formatScore(zoneScore));
        if (seasonScore > 0) {
            reasonBuilder.append(", 季节因子=").append(formatScore(seasonScore));
        }
        if (preferredScore > 0) {
            reasonBuilder.append(", 延续库位加分=").append(formatScore(preferredScore));
        }
        if (temperatureScore > 0) {
            reasonBuilder.append(", 温层匹配=").append(formatScore(temperatureScore));
        }
        if (insufficientCapacity) {
            reasonBuilder.append(", 容量不足，按最优候选回退");
        }

        LocationRecommendationResponse.Candidate candidate = new LocationRecommendationResponse.Candidate();
        candidate.setLocationCode(location.getCode());
        candidate.setCapacity(capacity);
        candidate.setCurrentLoad(currentLoad);
        candidate.setAvailableCapacity(availableCapacity);
        candidate.setScore(roundScore(totalScore));
        candidate.setReason(reasonBuilder.toString());
        return candidate;
    }

    private String formatScore(double score) {
        return String.format("%.1f", score);
    }

    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }
}
