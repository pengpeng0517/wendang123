package com.wms.modules.location.dto;

import com.wms.modules.location.enums.PolicyType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PolicyDecision {

    private String locationCode;

    private Integer capacity;

    private Integer currentLoad;

    private Integer availableCapacity;

    private Double score;

    private Double confidence;

    private String reason;

    private PolicyType policyType;

    private String policyVersion;

    private String fallbackReason;

    private List<LocationRecommendationResponse.Candidate> candidates = new ArrayList<>();
}
