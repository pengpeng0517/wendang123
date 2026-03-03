package com.wms.modules.location.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LocationPolicyStatusResponse {

    private String mode;

    private String modelVersion;

    private String modelPath;

    private boolean modelLoaded;

    private String modelLoadMessage;

    private LocalDateTime modelLastLoadTime;

    private Double confidenceThreshold;

    private Double shadowSampleRate;
}
