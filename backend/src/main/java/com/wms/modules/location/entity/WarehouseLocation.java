package com.wms.modules.location.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("warehouse_location")
public class WarehouseLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String zone;

    private Integer capacity;

    private Integer currentLoad;

    private Integer status;

    private Integer priority;

    private String temperatureLevel;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
