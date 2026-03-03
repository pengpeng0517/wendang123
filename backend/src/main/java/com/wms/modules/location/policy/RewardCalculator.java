package com.wms.modules.location.policy;

import com.wms.modules.location.entity.WarehouseLocation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RewardCalculator {

    private static final double W_CAPACITY_BALANCE = 0.35;
    private static final double W_PICK_COST = 0.25;
    private static final double W_TEMP_MATCH = 0.2;
    private static final double W_STABILITY = 0.1;
    private static final double W_OVERFLOW_PENALTY = 0.1;

    public RewardResult calculate(LocationPolicyContext context, WarehouseLocation selected) {
        if (selected == null) {
            RewardResult empty = new RewardResult();
            empty.setTotal(0.0);
            return empty;
        }

        int inboundQty = context.getInboundQuantity();
        int capacity = safeInt(selected.getCapacity());
        int currentLoad = safeInt(selected.getCurrentLoad());
        int available = capacity - currentLoad;

        double postRatio = capacity <= 0 ? 1.0 : (currentLoad + inboundQty) * 1.0 / capacity;
        double capacityBalance = Math.max(0.0, 1.0 - Math.abs(0.65 - postRatio));
        double pickCostNegative = zonePickCostScore(selected.getZone());
        double tempMatch = matchTemperature(selected.getTemperatureLevel(), context.getTemperatureLevel()) ? 1.0 : 0.0;
        double stability = matchCode(selected.getCode(), context.getPreferredLocation()) ? 1.0 : 0.0;
        double overflowPenalty = available >= inboundQty ? 0.0 : Math.min(1.0, (inboundQty - available) * 1.0 / Math.max(inboundQty, 1));

        double total = W_CAPACITY_BALANCE * capacityBalance
                + W_PICK_COST * pickCostNegative
                + W_TEMP_MATCH * tempMatch
                + W_STABILITY * stability
                - W_OVERFLOW_PENALTY * overflowPenalty;

        RewardResult result = new RewardResult();
        result.setTotal(round(total));

        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("capacity_balance", round(capacityBalance));
        breakdown.put("pick_cost_negative", round(pickCostNegative));
        breakdown.put("temperature_match", round(tempMatch));
        breakdown.put("stability", round(stability));
        breakdown.put("overflow_penalty", round(overflowPenalty));
        result.setBreakdown(breakdown);

        return result;
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

    private double zonePickCostScore(String zone) {
        if (zone == null) {
            return 0.5;
        }
        String normalized = zone.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("A")) {
            return 1.0;
        }
        if (normalized.startsWith("B")) {
            return 0.75;
        }
        if (normalized.startsWith("C")) {
            return 0.55;
        }
        return 0.4;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    public static class RewardResult {
        private double total;
        private Map<String, Double> breakdown = new LinkedHashMap<>();

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public Map<String, Double> getBreakdown() {
            return breakdown;
        }

        public void setBreakdown(Map<String, Double> breakdown) {
            this.breakdown = breakdown;
        }
    }
}
