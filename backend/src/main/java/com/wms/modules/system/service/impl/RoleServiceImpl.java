package com.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.system.entity.Role;
import com.wms.modules.system.mapper.RoleMapper;
import com.wms.modules.system.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> listRoles() {
        return list();
    }

    @Override
    public Role getRoleById(Long id) {
        return getById(id);
    }

    @Override
    public boolean saveRole(Role role) {
        return save(role);
    }

    @Override
    public boolean updateRole(Role role) {
        return updateById(role);
    }

    @Override
    public boolean removeRoleById(Long id) {
        return removeById(id);
    }
}