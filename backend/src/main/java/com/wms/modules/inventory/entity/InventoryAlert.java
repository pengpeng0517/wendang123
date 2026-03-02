package com.wms.modules.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_alert")
public class InventoryAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inventoryId;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String alertType;

    private String alertMessage;

    private Integer currentStock;

    private Integer threshold;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
