package com.wms.modules.location.dto;

import lombok.Data;

@Data
public class LocationRecommendationRequest {

    private Long materialId;

    private Integer inboundQuantity;

    private String preferredLocation;

    private String seasonTag;

    private String temperatureLevel;
}
