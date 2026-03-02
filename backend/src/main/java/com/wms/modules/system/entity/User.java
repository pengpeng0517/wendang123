package com.wms.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.wms.common.enums.RoleEnum;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("name")
    private String name;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 获取角色枚举
     */
    public RoleEnum getRoleEnum() {
        return RoleEnum.getByCode(this.role);
    }
}