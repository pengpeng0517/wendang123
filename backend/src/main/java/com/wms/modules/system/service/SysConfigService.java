package com.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.system.entity.SysConfig;
import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SysConfigService extends IService<SysConfig> {
    
    /**
     * 根据配置键获取配置值
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 获取所有启用的配置
     * @return 配置列表
     */
    List<SysConfig> getEnabledConfigs();
    
    /**
     * 批量更新配置
     * @param configs 配置列表
     * @return 是否更新成功
     */
    boolean updateBatchConfigs(List<SysConfig> configs);
}