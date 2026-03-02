package com.wms.modules.system.controller;

import com.wms.common.result.Result;
import com.wms.modules.system.entity.Role;
import com.wms.modules.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    public Result<List<Role>> list() {
        try {
            List<Role> roleList = roleService.listRoles();
            return Result.success("获取成功", roleList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取角色信息
     */
    @GetMapping("/info/{id}")
    public Result<Role> info(@PathVariable String id) {
        try {
            Long roleId = Long.parseLong(id);
            Role role = roleService.getRoleById(roleId);
            if (role == null) {
                return Result.error("角色不存在");
            }
            return Result.success("获取成功", role);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 新增角色
     */
    @PostMapping("/add")
    public Result<Role> add(@RequestBody Role role) {
        try {
            role.setStatus(1); // 默认启用
            boolean success = roleService.saveRole(role);
            if (success) {
                return Result.success("新增成功", role);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 修改角色
     */
    @PutMapping("/update")
    public Result<Role> update(@RequestBody Role role) {
        try {
            // 检查角色是否存在
            Role existingRole = roleService.getRoleById(role.getId());
            if (existingRole == null) {
                return Result.error("角色不存在");
            }

            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success("修改成功", role);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            Long roleId = Long.parseLong(id);
            boolean success = roleService.removeRoleById(roleId);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/status/{id}/{status}")
    public Result<String> status(@PathVariable String id, @PathVariable String status) {
        try {
            Long roleId = Long.parseLong(id);
            Integer roleStatus = Integer.parseInt(status);
            Role role = new Role();
            role.setId(roleId);
            role.setStatus(roleStatus);

            boolean success = roleService.updateRole(role);
            if (success) {
                return Result.success("操作成功");
            } else {
                return Result.error("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }
}