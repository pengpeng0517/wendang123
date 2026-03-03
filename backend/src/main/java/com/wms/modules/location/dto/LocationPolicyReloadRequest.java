package com.wms.modules.location.dto;

import lombok.Data;

@Data
public class LocationPolicyReloadRequest {

    private String modelPath;

    private String modelVersion;
}
