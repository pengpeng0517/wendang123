package com.wms.modules.system.controller;

import com.wms.common.result.Result;
import com.wms.modules.system.entity.SysConfig;
import com.wms.modules.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/system/config")
public class SysConfigController {
    
    @Autowired
    private SysConfigService sysConfigService;
    
    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public Result<List<SysConfig>> list() {
        List<SysConfig> configs = sysConfigService.list();
        return Result.success(configs);
    }
    
    /**
     * 根据ID获取配置
     */
    @GetMapping("/get/{id}")
    public Result<SysConfig> get(@PathVariable Long id) {
        SysConfig config = sysConfigService.getById(id);
        return Result.success(config);
    }
    
    /**
     * 新增配置
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SysConfig config) {
        boolean saved = sysConfigService.save(config);
        return saved ? Result.success() : Result.error("保存失败");
    }
    
    /**
     * 更新配置
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysConfig config) {
        boolean updated = sysConfigService.updateById(config);
        return updated ? Result.success() : Result.error("更新失败");
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = sysConfigService.removeById(id);
        return deleted ? Result.success() : Result.error("删除失败");
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batchUpdate")
    public Result<Void> batchUpdate(@RequestBody List<SysConfig> configs) {
        boolean updated = sysConfigService.updateBatchConfigs(configs);
        return updated ? Result.success() : Result.error("更新失败");
    }
    
    /**
     * 根据配置键获取配置值
     */
    @GetMapping("/getValue/{configKey}")
    public Result<String> getValue(@PathVariable String configKey) {
        String value = sysConfigService.getConfigValue(configKey);
        return Result.success(value);
    }
}