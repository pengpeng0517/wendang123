package com.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.system.entity.SysLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

/**
 * 系统日志服务接口
 */
public interface SysLogService extends IService<SysLog> {
    
    /**
     * 分页查询日志
     * @param page 页码
     * @param size 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    Page<SysLog> queryPage(int page, int size, Map<String, Object> params);
    
    /**
     * 清理日志
     * @param days 保留天数
     * @return 清理结果
     */
    boolean cleanLog(int days);
    
    /**
     * 记录操作日志
     * @param log 日志信息
     */
    void recordLog(SysLog log);
}