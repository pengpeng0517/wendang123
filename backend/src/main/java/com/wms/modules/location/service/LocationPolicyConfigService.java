package com.wms.modules.location.service;

import com.wms.modules.location.enums.PolicyMode;

public interface LocationPolicyConfigService {

    String KEY_MODE = "location.policy.mode";
    String KEY_MODEL_VERSION = "location.policy.modelVersion";
    String KEY_MODEL_PATH = "location.policy.modelPath";
    String KEY_CONFIDENCE_THRESHOLD = "location.policy.confidenceThreshold";
    String KEY_SHADOW_SAMPLE_RATE = "location.policy.shadowSampleRate";

    PolicyMode getPolicyMode();

    String getModelVersion();

    String getModelPath();

    double getConfidenceThreshold();

    double getShadowSampleRate();

    void updatePolicyMode(PolicyMode mode, String operator);

    void updateModelConfig(String modelPath, String modelVersion, String operator);
}
