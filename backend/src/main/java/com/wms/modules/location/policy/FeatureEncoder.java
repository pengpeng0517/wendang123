package com.wms.modules.location.policy;

import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.entity.WarehouseLocation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class FeatureEncoder {

    public float[] encode(LocationPolicyContext context, WarehouseLocation candidate) {
        LocationRecommendationRequest request = context.getRequest();
        int inboundQty = context.getInboundQuantity();
        int capacity = safeInt(candidate.getCapacity());
        int load = safeInt(candidate.getCurrentLoad());
        int available = capacity - load;

        double availableRatio = capacity <= 0 ? 0.0 : available * 1.0 / capacity;
        double loadRatio = capacity <= 0 ? 1.0 : load * 1.0 / capacity;
        double postLoadRatio = capacity <= 0 ? 1.0 : (load + inboundQty) * 1.0 / capacity;

        double zoneLevel = zoneLevel(candidate.getZone());
        double priority = Math.max(0, Math.min(safeInt(candidate.getPriority()), 10));
        double tempMatch = matchTemperature(candidate.getTemperatureLevel(), context.getTemperatureLevel()) ? 1.0 : 0.0;
        double preferredMatch = matchCode(candidate.getCode(), context.getPreferredLocation()) ? 1.0 : 0.0;
        double isPeakSeason = "旺季".equals(context.getSeasonTag()) ? 1.0 : 0.0;
        double isLowSeason = "淡季".equals(context.getSeasonTag()) ? 1.0 : 0.0;

        int locationCount = context.getActiveLocations() == null ? 0 : context.getActiveLocations().size();
        int candidateRankProxy = candidateRankProxy(candidate, context.getActiveLocations());

        float[] features = new float[16];
        features[0] = (float) Math.min(1.0, inboundQty / 1000.0);
        features[1] = (float) Math.min(1.0, capacity / 2000.0);
        features[2] = (float) Math.min(1.0, Math.max(0.0, load) / 2000.0);
        features[3] = (float) Math.max(-1.0, Math.min(1.0, availableRatio));
        features[4] = (float) Math.max(-1.0, Math.min(1.5, loadRatio));
        features[5] = (float) Math.max(-1.0, Math.min(2.0, postLoadRatio));
        features[6] = (float) zoneLevel;
        features[7] = (float) (priority / 10.0);
        features[8] = (float) tempMatch;
        features[9] = (float) preferredMatch;
        features[10] = (float) isPeakSeason;
        features[11] = (float) isLowSeason;
        features[12] = request != null && request.getMaterialId() != null ? (float) (request.getMaterialId() % 1024) / 1024.0f : 0.0f;
        features[13] = (float) Math.min(1.0, locationCount / 200.0);
        features[14] = (float) Math.min(1.0, candidateRankProxy / 100.0);
        features[15] = available >= inboundQty ? 1.0f : 0.0f;

        return features;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean matchCode(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return left.trim().equalsIgnoreCase(right.trim());
    }

    private boolean matchTemperature(String locationTemperature, String requestTemperature) {
        if (requestTemperature == null || requestTemperature.trim().isEmpty()) {
            return true;
        }
        if (locationTemperature == null || locationTemperature.trim().isEmpty()) {
            return true;
        }
        return locationTemperature.trim().equalsIgnoreCase(requestTemperature.trim());
    }

    private double zoneLevel(String zone) {
        if (zone == null) {
            return 0.2;
        }
        String normalized = zone.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("A")) {
            return 1.0;
        }
        if (normalized.startsWith("B")) {
            return 0.7;
        }
        if (normalized.startsWith("C")) {
            return 0.45;
        }
        return 0.2;
    }

    private int candidateRankProxy(WarehouseLocation candidate, List<WarehouseLocation> activeLocations) {
        if (activeLocations == null || activeLocations.isEmpty()) {
            return 0;
        }
        int greaterCount = 0;
        int targetPriority = safeInt(candidate.getPriority());
        for (WarehouseLocation location : activeLocations) {
            if (safeInt(location.getPriority()) > targetPriority) {
                greaterCount++;
            }
        }
        return activeLocations.size() - greaterCount;
    }
}
