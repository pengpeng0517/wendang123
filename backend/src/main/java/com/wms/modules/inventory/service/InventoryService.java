package com.wms.modules.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.inventory.entity.Inventory;

import java.util.List;

public interface InventoryService extends IService<Inventory> {

    List<Inventory> listInventory(String keyword, Long materialId);

    Inventory getInventoryByMaterialId(Long materialId);

    boolean addStock(Long materialId, Integer quantity, String operator);

    boolean addStock(Long materialId, Integer quantity, String operator, String locationCode);

    boolean reduceStock(Long materialId, Integer quantity, String operator);

    boolean reduceStock(Long materialId, Integer quantity, String operator, String locationCode);

    boolean checkStock(Long materialId, Integer quantity);
}
