package com.wms.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.modules.system.entity.RolePermission;

import java.util.List;

public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    /**
     * 根据角色ID获取权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(Long roleId);

    /**
     * 根据角色ID删除角色权限关联
     */
    int deleteByRoleId(Long roleId);
}