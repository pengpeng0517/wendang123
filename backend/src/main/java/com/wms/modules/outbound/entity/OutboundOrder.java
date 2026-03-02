package com.wms.modules.outbound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("outbound_order")
public class OutboundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String type;

    private Long recipientId;

    private String recipientName;

    private Integer totalQuantity;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String createUserId;

    private String createUserName;

    @TableField(exist = false)
    private List<OutboundDetail> details;
}
