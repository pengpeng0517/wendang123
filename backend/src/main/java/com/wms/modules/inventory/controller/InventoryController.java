package com.wms.modules.inventory.controller;

import com.wms.common.result.Result;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.entity.InventoryAlert;
import com.wms.modules.inventory.service.InventoryAlertService;
import com.wms.modules.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryAlertService inventoryAlertService;

    @GetMapping("/list")
    public Result<List<Inventory>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long materialId) {
        try {
            List<Inventory> inventoryList = inventoryService.listInventory(keyword, materialId);
            return Result.success("获取成功", inventoryList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Inventory> info(@PathVariable Long id) {
        try {
            Inventory inventory = inventoryService.getById(id);
            if (inventory == null) {
                return Result.error("库存记录不存在");
            }
            return Result.success("获取成功", inventory);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Inventory> add(@RequestBody Inventory inventory) {
        try {
            inventory.setCreateTime(java.time.LocalDateTime.now());
            inventory.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = inventoryService.save(inventory);
            if (success) {
                return Result.success("新增成功", inventory);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Inventory> update(@RequestBody Inventory inventory) {
        try {
            if (inventory.getId() == null) {
                return Result.error("库存记录ID不能为空");
            }
            inventory.setUpdateTime(java.time.LocalDateTime.now());
            boolean success = inventoryService.updateById(inventory);
            if (success) {
                return Result.success("修改成功", inventory);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean success = inventoryService.removeById(id);
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

    @GetMapping("/alerts")
    public Result<List<InventoryAlert>> listAlerts(
            @RequestParam(required = false) Integer status) {
        try {
            List<InventoryAlert> alertList = inventoryAlertService.listAlerts(status);
            return Result.success("获取成功", alertList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PutMapping("/alert/{id}/status")
    public Result<String> updateAlertStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        try {
            boolean success = inventoryAlertService.updateAlertStatus(id, status);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }
}
