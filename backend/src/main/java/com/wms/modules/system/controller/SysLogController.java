package com.wms.modules.system.controller;

import com.wms.common.result.Result;
import com.wms.modules.system.entity.SysLog;
import com.wms.modules.system.service.SysLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 系统日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
public class SysLogController {
    
    @Autowired
    private SysLogService sysLogService;
    
    /**
     * 分页查询日志
     */
    @GetMapping("/list")
    public Result<Page<SysLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status) {
        
        Map<String, Object> params = new java.util.HashMap<>();
        if (username != null) params.put("username", username);
        if (operation != null) params.put("operation", operation);
        if (status != null) params.put("status", status);
        
        Page<SysLog> logPage = sysLogService.queryPage(page, size, params);
        return Result.success(logPage);
    }
    
    /**
     * 清理日志
     */
    @DeleteMapping("/clean")
    public Result<Void> clean(@RequestParam(defaultValue = "30") int days) {
        boolean cleaned = sysLogService.cleanLog(days);
        return cleaned ? Result.success() : Result.error("清理失败");
    }
    
    /**
     * 记录日志
     */
    @PostMapping("/record")
    public Result<Void> record(@RequestBody SysLog log) {
        sysLogService.recordLog(log);
        return Result.success();
    }
}