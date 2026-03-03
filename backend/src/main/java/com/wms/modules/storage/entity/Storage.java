package com.wms.modules.storage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("storage")
public class Storage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private Integer quantity;
    private String batchNumber;
    private String location;
    private LocalDateTime storageTime;
    private String operator;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String policyTraceId;
}
