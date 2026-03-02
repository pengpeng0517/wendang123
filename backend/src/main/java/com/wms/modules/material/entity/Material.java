package com.wms.modules.material.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material")
public class Material {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private Long categoryId;

    private String spec;

    private String unit;

    private Long supplierId;

    private BigDecimal price;

    private Integer minStock;

    private Integer maxStock;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUserId;

    private Long updateUserId;
}
