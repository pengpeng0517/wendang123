package com.wms.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 */
@Data
@TableName("sys_log")
public class SysLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作用户
     */
    private String username;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 请求参数
     */
    private String requestParams;
    
    /**
     * 响应结果
     */
    private String responseResult;
    
    /**
     * 操作状态：1-成功，0-失败
     */
    private Integer status;
    
    /**
     * 操作IP
     */
    private String ip;
    
    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
    
    /**
     * 执行时长（毫秒）
     */
    private Long executeTime;
}
