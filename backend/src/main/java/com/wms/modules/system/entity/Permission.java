package com.wms.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("sys_permission")
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField("name")
    private String name;

    /**
     * 权限编码
     */
    @TableField("code")
    private String code;

    /**
     * 权限类型：menu-菜单，button-按钮
     */
    @TableField("type")
    private String type;

    /**
     * 权限URL
     */
    @TableField("url")
    private String url;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;
}