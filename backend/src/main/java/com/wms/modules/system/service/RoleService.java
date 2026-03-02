package com.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.system.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    /**
     * 获取所有角色
     */
    List<Role> listRoles();

    /**
     * 根据ID获取角色
     */
    Role getRoleById(Long id);

    /**
     * 新增角色
     */
    boolean saveRole(Role role);

    /**
     * 更新角色
     */
    boolean updateRole(Role role);

    /**
     * 删除角色
     */
    boolean removeRoleById(Long id);
}