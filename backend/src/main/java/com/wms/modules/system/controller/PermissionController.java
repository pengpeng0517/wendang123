package com.wms.modules.system.controller;

import com.wms.common.result.Result;
import com.wms.modules.system.entity.Permission;
import com.wms.modules.system.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取权限列表
     */
    @GetMapping("/list")
    public Result<List<Permission>> list() {
        try {
            List<Permission> permissionList = permissionService.listPermissions();
            return Result.success("获取成功", permissionList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 根据角色ID获取权限列表
     */
    @GetMapping("/listByRoleId/{roleId}")
    public Result<List<Permission>> listByRoleId(@PathVariable Long roleId) {
        try {
            List<Permission> permissionList = permissionService.listPermissionsByRoleId(roleId);
            return Result.success("获取成功", permissionList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 分配权限给角色
     */
    @PostMapping("/assign")
    public Result<String> assign(@RequestBody PermissionAssignRequest request) {
        try {
            boolean success = permissionService.assignPermissions(request.getRoleId(), request.getPermissionIds());
            if (success) {
                return Result.success("权限分配成功");
            } else {
                return Result.error("权限分配失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    /**
     * 权限分配请求参数
     */
    public static class PermissionAssignRequest {
        private Long roleId;
        private List<Long> permissionIds;

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public List<Long> getPermissionIds() {
            return permissionIds;
        }

        public void setPermissionIds(List<Long> permissionIds) {
            this.permissionIds = permissionIds;
        }
    }
}