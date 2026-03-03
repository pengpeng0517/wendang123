package com.wms.modules.location.service;

import com.wms.modules.location.dto.LocationPolicyEvaluationResponse;
import com.wms.modules.location.dto.LocationPolicyStatusResponse;
import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.dto.LocationRecommendationResponse;

import java.time.LocalDateTime;

public interface LocationAllocationService {

    LocationRecommendationResponse recommend(LocationRecommendationRequest request);

    void recordAllocation(Long materialId,
                          String materialCode,
                          String materialName,
                          Integer inboundQuantity,
                          String operator,
                          LocationRecommendationRequest request,
                          LocationRecommendationResponse response);

    String getCurrentPolicyMode();

    LocationPolicyStatusResponse getPolicyStatus();

    boolean reloadPolicyModel(String modelPath, String modelVersion, String operator);

    void updatePolicyMode(String mode, String operator);

    LocationPolicyEvaluationResponse evaluatePolicy(LocalDateTime from, LocalDateTime to);

    void completePolicyEvent(String traceId, String executedActionCode, String operator);
}
