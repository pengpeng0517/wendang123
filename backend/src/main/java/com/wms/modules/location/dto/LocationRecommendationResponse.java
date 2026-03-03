package com.wms.modules.location.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LocationRecommendationResponse {

    private String locationCode;

    private Integer capacity;

    private Integer currentLoad;

    private Integer availableCapacity;

    private Double score;

    private String reason;

    private String policyMode;

    private String policyType;

    private String policyVersion;

    private Double confidence;

    private String fallbackReason;

    private String traceId;

    private List<Candidate> candidates = new ArrayList<Candidate>();

    @Data
    public static class Candidate {
        private String locationCode;
        private Integer capacity;
        private Integer currentLoad;
        private Integer availableCapacity;
        private Double score;
        private String reason;
    }
}
