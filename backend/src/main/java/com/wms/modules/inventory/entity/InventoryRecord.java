package com.wms.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inventoryId;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String type;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal amount;

    private String batchNumber;

    private String operator;

    private LocalDateTime createTime;
}
