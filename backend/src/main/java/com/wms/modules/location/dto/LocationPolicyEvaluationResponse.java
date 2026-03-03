package com.wms.modules.location.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class LocationPolicyEvaluationResponse {

    private LocalDateTime from;

    private LocalDateTime to;

    private List<LocationPolicyEvaluationItem> items = new ArrayList<>();
}
