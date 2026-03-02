package com.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.system.entity.Permission;
import com.wms.modules.system.entity.RolePermission;
import com.wms.modules.system.mapper.PermissionMapper;
import com.wms.modules.system.mapper.RolePermissionMapper;
import com.wms.modules.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public List<Permission> listPermissions() {
        return list();
    }

    @Override
    public List<Permission> listPermissionsByRoleId(Long roleId) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return listByIds(permissionIds);
    }

    @Transactional
    @Override
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除旧的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 再添加新的权限关联
        if (!permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }

        return true;
    }
}