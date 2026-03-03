package com.wms.modules.location.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.modules.location.enums.PolicyMode;
import com.wms.modules.location.service.LocationPolicyConfigService;
import com.wms.modules.system.entity.SysConfig;
import com.wms.modules.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LocationPolicyConfigServiceImpl implements LocationPolicyConfigService {

    @Autowired
    private SysConfigService sysConfigService;

    @Override
    public PolicyMode getPolicyMode() {
        String mode = getConfigValue(KEY_MODE, "rule");
        return PolicyMode.fromValue(mode);
    }

    @Override
    public String getModelVersion() {
        return getConfigValue(KEY_MODEL_VERSION, "none");
    }

    @Override
    public String getModelPath() {
        return getConfigValue(KEY_MODEL_PATH, "/opt/wendang123/models/location-rl/current/model.onnx");
    }

    @Override
    public double getConfidenceThreshold() {
        return parseDouble(getConfigValue(KEY_CONFIDENCE_THRESHOLD, "0.65"), 0.65);
    }

    @Override
    public double getShadowSampleRate() {
        return parseDouble(getConfigValue(KEY_SHADOW_SAMPLE_RATE, "1.0"), 1.0);
    }

    @Override
    public void updatePolicyMode(PolicyMode mode, String operator) {
        upsert(KEY_MODE,
                mode == null ? PolicyMode.RULE.getValue() : mode.getValue(),
                "库位策略模式",
                "rule|shadow|manual|auto",
                operator);
    }

    @Override
    public void updateModelConfig(String modelPath, String modelVersion, String operator) {
        if (modelPath != null && !modelPath.trim().isEmpty()) {
            upsert(KEY_MODEL_PATH,
                    modelPath.trim(),
                    "库位RL模型路径",
                    "ONNX模型文件路径",
                    operator);
        }
        if (modelVersion != null && !modelVersion.trim().isEmpty()) {
            upsert(KEY_MODEL_VERSION,
                    modelVersion.trim(),
                    "库位RL模型版本",
                    "模型版本号",
                    operator);
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        SysConfig config = getByKey(key);
        if (config == null || config.getConfigValue() == null || config.getConfigValue().trim().isEmpty()) {
            return defaultValue;
        }
        return config.getConfigValue().trim();
    }

    private SysConfig getByKey(String key) {
        QueryWrapper<SysConfig> wrapper = new QueryWrapper<SysConfig>();
        wrapper.eq("config_key", key);
        return sysConfigService.getOne(wrapper);
    }

    private void upsert(String key, String value, String name, String description, String operator) {
        SysConfig config = getByKey(key);
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigName(name);
            config.setDescription(description);
            config.setStatus(1);
            config.setCreateTime(LocalDateTime.now());
            config.setCreateBy(defaultOperator(operator));
        }

        config.setConfigValue(value);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(defaultOperator(operator));

        if (config.getId() == null) {
            sysConfigService.save(config);
        } else {
            sysConfigService.updateById(config);
        }
    }

    private String defaultOperator(String operator) {
        if (operator == null || operator.trim().isEmpty()) {
            return "system";
        }
        return operator.trim();
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
