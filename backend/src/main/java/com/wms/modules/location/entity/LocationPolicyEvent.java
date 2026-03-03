package com.wms.modules.location.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("location_policy_event")
public class LocationPolicyEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime eventTime;

    private Long materialId;

    private String materialCode;

    private Long categoryId;

    private Integer inboundQty;

    private String candidateSetJson;

    private String selectedActionCode;

    private String executedActionCode;

    private String policyMode;

    private String policyType;

    private String policyVersion;

    private String stateVectorJson;

    private Double rewardTotal;

    private String rewardBreakdownJson;

    private Integer isFallback;

    private Double confidence;

    private String operator;

    private String traceId;
}
