package com.wms.modules.outbound.controller;

import com.wms.common.result.Result;
import com.wms.modules.outbound.entity.OutboundOrder;
import com.wms.modules.outbound.service.OutboundOrderService;
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
@RequestMapping("/api/outbound/order")
public class OutboundOrderController {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.wms.modules.inventory.service.InventoryService inventoryService;
    
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
            log.setRequestUrl("/api/outbound/order" + operation.toLowerCase());
            log.setRequestMethod("POST");
            log.setStatus(status);
            log.setIp("127.0.0.1"); // 实际应用中应获取真实IP
            log.setExecuteTime(0L);
            sysLogService.recordLog(log);
        }
    }

    @GetMapping("/list")
    public Result<List<OutboundOrder>> list(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer status) {
        List<OutboundOrder> list = outboundOrderService.getOrderList(keyword, status);
        return Result.success(list);
    }

    @GetMapping("/info/{id}")
    public Result<OutboundOrder> info(@PathVariable Long id) {
        OutboundOrder order = outboundOrderService.getOrderById(id);
        return Result.success(order);
    }

    @PostMapping("/add")
    public Result<OutboundOrder> add(@RequestBody OutboundOrder order) {
        try {
            OutboundOrder createdOrder = outboundOrderService.createOrder(order);
            // 记录日志
            recordLog("出库操作", "创建出库单：" + createdOrder.getOrderNo(), 1);
            return Result.success(createdOrder);
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("出库操作", "创建出库单异常：" + e.getMessage(), 0);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<OutboundOrder> update(@RequestBody OutboundOrder order) {
        try {
            OutboundOrder updatedOrder = outboundOrderService.updateOrder(order);
            // 记录日志
            recordLog("出库操作", "更新出库单：" + order.getOrderNo(), 1);
            return Result.success(updatedOrder);
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("出库操作", "更新出库单异常：" + e.getMessage(), 0);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            // 只有系统管理员可以删除
            if (!isSystemAdmin()) {
                return Result.error("只有系统管理员可以删除出库单");
            }
            
            boolean deleted = outboundOrderService.deleteOrder(id);
            if (deleted) {
                // 记录日志
                recordLog("出库操作", "删除出库单：ID=" + id, 1);
                return Result.success();
            } else {
                // 记录日志
                recordLog("出库操作", "删除出库单失败：ID=" + id, 0);
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("出库操作", "删除出库单异常：" + e.getMessage(), 0);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/approve/{id}")
    public Result<OutboundOrder> approve(@PathVariable Long id) {
        try {
            // 只有系统管理员可以批准
            if (!isSystemAdmin()) {
                return Result.error("只有系统管理员可以批准出库单");
            }
            
            OutboundOrder order = outboundOrderService.approveOrder(id);
            if (order == null) {
                // 记录日志
                recordLog("出库操作", "批准出库单失败：ID=" + id, 0);
                return Result.error("出库单不存在或状态不正确");
            }
            
            // 记录日志
            recordLog("出库操作", "批准出库单：" + order.getOrderNo(), 1);
            return Result.success(order);
        } catch (Exception e) {
            e.printStackTrace();
            // 记录日志
            recordLog("出库操作", "批准出库单异常：" + e.getMessage(), 0);
            return Result.error("批准失败：" + e.getMessage());
        }
    }
}
