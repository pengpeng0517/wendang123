package com.wms.modules.location.dto;

import lombok.Data;

@Data
public class LocationPolicyEvaluationItem {

    private String policyType;

    private Long totalCount;

    private Long fallbackCount;

    private Double fallbackRate;

    private Double avgReward;

    private Double avgConfidence;
}
