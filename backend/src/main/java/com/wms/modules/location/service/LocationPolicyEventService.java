package com.wms.modules.location.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.location.dto.LocationPolicyEvaluationItem;
import com.wms.modules.location.entity.LocationPolicyEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface LocationPolicyEventService extends IService<LocationPolicyEvent> {

    void saveEventAsync(LocationPolicyEvent event);

    void completeEvent(String traceId, String executedActionCode, String operator);

    List<LocationPolicyEvaluationItem> evaluate(LocalDateTime from, LocalDateTime to);
}
