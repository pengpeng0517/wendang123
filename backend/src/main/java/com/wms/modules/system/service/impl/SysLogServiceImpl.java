package com.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.modules.system.entity.SysLog;
import com.wms.modules.system.mapper.SysLogMapper;
import com.wms.modules.system.service.SysLogService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 系统日志服务实现
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
    
    @Override
    public Page<SysLog> queryPage(int page, int size, Map<String, Object> params) {
        Page<SysLog> pageParam = new Page<>(page, size);
        
        return lambdaQuery()
                .like(params.containsKey("username"), SysLog::getUsername, params.get("username"))
                .like(params.containsKey("operation"), SysLog::getOperation, params.get("operation"))
                .eq(params.containsKey("status"), SysLog::getStatus, params.get("status"))
                .orderByDesc(SysLog::getOperateTime)
                .page(pageParam);
    }
    
    @Override
    public boolean cleanLog(int days) {
        LocalDateTime time = LocalDateTime.now().minusDays(days);
        return remove(new QueryWrapper<SysLog>().lt("operate_time", time));
    }
    
    @Override
    public void recordLog(SysLog log) {
        log.setOperateTime(LocalDateTime.now());
        save(log);
    }
}