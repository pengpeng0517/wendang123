package com.wms.modules.location.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("location_allocation_log")
public class LocationAllocationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private Integer inboundQuantity;

    private String locationCode;

    private Double score;

    private String reason;

    private String seasonTag;

    private String policyType;

    private String policyVersion;

    private Double confidence;

    private String fallbackReason;

    private Long latencyMs;

    private String traceId;

    private String operator;

    private LocalDateTime createTime;
}
