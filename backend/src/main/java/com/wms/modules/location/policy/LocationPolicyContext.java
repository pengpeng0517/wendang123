package com.wms.modules.location.policy;

import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.entity.WarehouseLocation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LocationPolicyContext {

    private LocationRecommendationRequest request;

    private int inboundQuantity;

    private String preferredLocation;

    private String seasonTag;

    private String temperatureLevel;

    private List<WarehouseLocation> activeLocations = new ArrayList<>();

    private String traceId;
}
