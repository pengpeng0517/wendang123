package com.wms.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String spec;

    private String unit;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private String location;

    private Integer minStock;

    private Integer maxStock;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
