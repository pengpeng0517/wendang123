package com.wms.modules.outbound.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("outbound_detail")
public class OutboundDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String spec;

    private String unit;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private String batchNumber;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
