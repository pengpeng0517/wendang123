package com.wms.modules.location.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.location.dto.LocationPolicyEvaluationItem;
import com.wms.modules.location.entity.LocationPolicyEvent;
import com.wms.modules.location.mapper.LocationPolicyEventMapper;
import com.wms.modules.location.service.LocationPolicyEventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationPolicyEventServiceImpl extends ServiceImpl<LocationPolicyEventMapper, LocationPolicyEvent> implements LocationPolicyEventService {

    @Override
    public void saveEventAsync(final LocationPolicyEvent event) {
        if (event == null) {
            return;
        }
        save(event);
    }

    @Override
    public void completeEvent(String traceId, String executedActionCode, String operator) {
        if (traceId == null || traceId.trim().isEmpty()) {
            return;
        }

        LambdaQueryWrapper<LocationPolicyEvent> wrapper = new LambdaQueryWrapper<LocationPolicyEvent>();
        wrapper.eq(LocationPolicyEvent::getTraceId, traceId.trim())
                .orderByDesc(LocationPolicyEvent::getId)
                .last("limit 1");

        LocationPolicyEvent event = getOne(wrapper);
        if (event == null) {
            return;
        }

        event.setExecutedActionCode(executedActionCode);
        if (operator != null && !operator.trim().isEmpty()) {
            event.setOperator(operator.trim());
        }
        updateById(event);
    }

    @Override
    public List<LocationPolicyEvaluationItem> evaluate(LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<LocationPolicyEvent> wrapper = new LambdaQueryWrapper<LocationPolicyEvent>();
        if (from != null) {
            wrapper.ge(LocationPolicyEvent::getEventTime, from);
        }
        if (to != null) {
            wrapper.le(LocationPolicyEvent::getEventTime, to);
        }

        List<LocationPolicyEvent> events = list(wrapper);
        Map<String, Aggregate> aggregateMap = new LinkedHashMap<String, Aggregate>();

        for (LocationPolicyEvent event : events) {
            String key = event.getPolicyType();
            if (key == null || key.trim().isEmpty()) {
                key = "unknown";
            }

            Aggregate aggregate = aggregateMap.get(key);
            if (aggregate == null) {
                aggregate = new Aggregate();
                aggregateMap.put(key, aggregate);
            }

            aggregate.totalCount++;
            if (event.getIsFallback() != null && event.getIsFallback() == 1) {
                aggregate.fallbackCount++;
            }
            if (event.getRewardTotal() != null) {
                aggregate.rewardTotal += event.getRewardTotal();
                aggregate.rewardCount++;
            }
            if (event.getConfidence() != null) {
                aggregate.confidenceTotal += event.getConfidence();
                aggregate.confidenceCount++;
            }
        }

        List<LocationPolicyEvaluationItem> items = new ArrayList<LocationPolicyEvaluationItem>();
        for (Map.Entry<String, Aggregate> entry : aggregateMap.entrySet()) {
            Aggregate aggregate = entry.getValue();
            LocationPolicyEvaluationItem item = new LocationPolicyEvaluationItem();
            item.setPolicyType(entry.getKey());
            item.setTotalCount(aggregate.totalCount);
            item.setFallbackCount(aggregate.fallbackCount);

            double fallbackRate = aggregate.totalCount == 0 ? 0.0 : aggregate.fallbackCount * 1.0 / aggregate.totalCount;
            item.setFallbackRate(round(fallbackRate));
            item.setAvgReward(aggregate.rewardCount == 0 ? 0.0 : round(aggregate.rewardTotal / aggregate.rewardCount));
            item.setAvgConfidence(aggregate.confidenceCount == 0 ? 0.0 : round(aggregate.confidenceTotal / aggregate.confidenceCount));
            items.add(item);
        }

        return items;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static class Aggregate {
        private long totalCount;
        private long fallbackCount;
        private double rewardTotal;
        private long rewardCount;
        private double confidenceTotal;
        private long confidenceCount;
    }
}
