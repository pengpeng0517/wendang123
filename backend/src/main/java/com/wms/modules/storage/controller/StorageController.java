package com.wms.modules.storage.controller;

import com.wms.common.result.Result;
import com.wms.modules.storage.entity.Storage;
import com.wms.modules.storage.service.StorageService;
import com.wms.modules.system.entity.SysLog;
import com.wms.modules.system.entity.User;
import com.wms.modules.system.service.SysLogService;
import com.wms.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private SysLogService sysLogService;

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                return ((org.springframework.security.core.userdetails.User) principal).getUsername();
            }
            return principal.toString();
        }
        return null;
    }

    /**
     * 判断当前用户是否为系统管理员
     */
    private boolean isSystemAdmin() {
        String username = getCurrentUsername();
        if (username == null) {
            return false;
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        return "system_admin".equals(role) || "admin".equals(role);
    }
    
    /**
     * 记录系统日志
     */
    private void recordLog(String operation, String description, int status) {
        String username = getCurrentUsername();
        if (username != null) {
            SysLog log = new SysLog();
            log.setUsername(username);
            log.setOperation(operation);
            log.setDescription(description);
            log.setRequestUrl("/storage" + operation.toLowerCase());
            log.setRequestMethod("POST");
            log.setStatus(status);
            log.setIp("127.0.0.1"); // 实际应用中应获取真实IP
            log.setExecuteTime(0L);
            sysLogService.recordLog(log);
        }
    }

    @GetMapping("/list")
    public Result<List<Storage>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String batchNumber) {
        try {
            List<Storage> storageList = storageService.listStorages(keyword, materialId, batchNumber);
            return Result.success("获取成功", storageList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Storage> info(@PathVariable Long id) {
        try {
            Storage storage = storageService.getStorageById(id);
            if (storage == null) {
                return Result.error("入库记录不存在");
            }
            return Result.success("获取成功", storage);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Storage> add(@RequestBody Storage storage) {
        try {
            boolean success = storageService.saveStorage(storage);
            if (success) {
                // 记录日志
                recordLog("入库操作", "新增入库记录：" + storage.getMaterialName(), 1);
                return Result.success("新增成功", storage);
            } else {
                // 记录日志
                recordLog("入库操作", "新增入库记录失败", 0);
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("入库操作", "新增入库记录异常：" + e.getMessage(), 0);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Storage> update(@RequestBody Storage storage) {
        try {
            if (storage.getId() == null) {
                return Result.error("入库记录ID不能为空");
            }
            boolean success = storageService.updateStorage(storage);
            if (success) {
                // 记录日志
                recordLog("入库操作", "更新入库记录：" + storage.getMaterialName(), 1);
                return Result.success("修改成功", storage);
            } else {
                // 记录日志
                recordLog("入库操作", "更新入库记录失败", 0);
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("入库操作", "更新入库记录异常：" + e.getMessage(), 0);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            // 只有系统管理员才能删除入库记录
            if (!isSystemAdmin()) {
                return Result.error("只有系统管理员才能删除入库记录");
            }
            
            boolean success = storageService.removeStorage(id);
            if (success) {
                // 记录日志
                recordLog("入库操作", "删除入库记录：ID=" + id, 1);
                return Result.success("删除成功");
            } else {
                // 记录日志
                recordLog("入库操作", "删除入库记录失败：ID=" + id, 0);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("入库操作", "删除入库记录异常：" + e.getMessage(), 0);
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

}
