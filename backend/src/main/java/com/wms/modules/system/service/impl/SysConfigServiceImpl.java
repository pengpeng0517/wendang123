package com.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.system.entity.SysConfig;
import com.wms.modules.system.mapper.SysConfigMapper;
import com.wms.modules.system.service.SysConfigService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 系统配置服务实现
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {
    
    @Override
    public String getConfigValue(String configKey) {
        SysConfig config = lambdaQuery()
                .eq(SysConfig::getConfigKey, configKey)
                .eq(SysConfig::getStatus, 1)
                .one();
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public List<SysConfig> getEnabledConfigs() {
        return lambdaQuery()
                .eq(SysConfig::getStatus, 1)
                .list();
    }
    
    @Override
    public boolean updateBatchConfigs(List<SysConfig> configs) {
        return updateBatchById(configs);
    }
}