package com.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.system.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    /**
     * 获取所有权限
     */
    List<Permission> listPermissions();

    /**
     * 根据角色ID获取权限列表
     */
    List<Permission> listPermissionsByRoleId(Long roleId);

    /**
     * 分配权限给角色
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);
}