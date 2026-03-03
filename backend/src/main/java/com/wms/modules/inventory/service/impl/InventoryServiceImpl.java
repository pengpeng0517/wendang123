package com.wms.modules.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.entity.InventoryRecord;
import com.wms.modules.inventory.mapper.InventoryMapper;
import com.wms.modules.inventory.service.InventoryRecordService;
import com.wms.modules.inventory.service.InventoryService;
import com.wms.modules.location.service.WarehouseLocationService;
import com.wms.modules.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private InventoryRecordService inventoryRecordService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Override
    public List<Inventory> listInventory(String keyword, Long materialId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Inventory::getMaterialCode, keyword)
                    .or()
                    .like(Inventory::getMaterialName, keyword));
        }

        if (materialId != null) {
            wrapper.eq(Inventory::getMaterialId, materialId);
        }

        wrapper.orderByAsc(Inventory::getMaterialCode);

        List<Inventory> inventoryList = list(wrapper);
        // 对历史数据做轻量自修复：若颜色等快照字段与物料主数据不一致则同步
        for (Inventory inventory : inventoryList) {
            syncMaterialSnapshot(inventory);
        }
        return inventoryList;
    }

    @Override
    public Inventory getInventoryByMaterialId(Long materialId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getMaterialId, materialId);
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean addStock(Long materialId, Integer quantity, String operator) {
        return addStock(materialId, quantity, operator, null);
    }

    @Override
    @Transactional
    public boolean addStock(Long materialId, Integer quantity, String operator, String locationCode) {
        if (materialId == null || quantity == null || quantity <= 0) {
            return false;
        }

        Inventory inventory = getInventoryByMaterialId(materialId);
        String requestedLocation = normalizeLocation(locationCode);

        if (inventory == null) {
            com.wms.modules.material.entity.Material material = materialService.getById(materialId);
            if (material == null) {
                return false;
            }

            inventory = new Inventory();
            inventory.setMaterialId(materialId);
            inventory.setMaterialCode(material.getCode());
            inventory.setMaterialName(material.getName());
            inventory.setSpec(material.getSpec());
            inventory.setUnit(material.getUnit());
            inventory.setQuantity(quantity);
            inventory.setPrice(material.getPrice() != null ? material.getPrice() : BigDecimal.ZERO);
            inventory.setAmount(inventory.getPrice().multiply(BigDecimal.valueOf(quantity)));
            inventory.setLocation(requestedLocation == null ? "" : requestedLocation);
            inventory.setMinStock(material.getMinStock() != null ? material.getMinStock() : 0);
            inventory.setMaxStock(material.getMaxStock() != null ? material.getMaxStock() : 0);
            inventory.setStatus(1);
            inventory.setCreateTime(LocalDateTime.now());
            inventory.setUpdateTime(LocalDateTime.now());
            save(inventory);
        } else {
            syncMaterialSnapshot(inventory);
            String oldLocation = normalizeLocation(inventory.getLocation());
            int oldQuantity = inventory.getQuantity() == null ? 0 : inventory.getQuantity();

            inventory.setQuantity(oldQuantity + quantity);

            // 扩展策略：若显式传入新库位且与现有库位不同，则视作一次“库位迁移”
            // 为保证模型一致性，同时迁移当前已存量的库位载荷。
            if (requestedLocation != null && !requestedLocation.equalsIgnoreCase(oldLocation)) {
                inventory.setLocation(requestedLocation);
                if (oldQuantity > 0) {
                    assertLocationChange(warehouseLocationService.decreaseLoad(oldLocation, oldQuantity), "旧库位载荷回退失败");
                    assertLocationChange(warehouseLocationService.increaseLoad(requestedLocation, oldQuantity), "新库位载荷迁移失败");
                }
            }

            BigDecimal price = inventory.getPrice() == null ? BigDecimal.ZERO : inventory.getPrice();
            inventory.setAmount(price.multiply(BigDecimal.valueOf(inventory.getQuantity())));
            inventory.setUpdateTime(LocalDateTime.now());
            updateById(inventory);
        }

        String finalLocation = normalizeLocation(inventory.getLocation());
        if (finalLocation != null) {
            assertLocationChange(warehouseLocationService.increaseLoad(finalLocation, quantity), "库位容量不足，入库失败");
        }

        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setMaterialId(materialId);
        record.setMaterialCode(inventory.getMaterialCode());
        record.setMaterialName(inventory.getMaterialName());
        record.setType("入库");
        record.setQuantity(quantity);
        BigDecimal recordPrice = inventory.getPrice() == null ? BigDecimal.ZERO : inventory.getPrice();
        record.setPrice(recordPrice);
        record.setAmount(recordPrice.multiply(BigDecimal.valueOf(quantity)));
        record.setOperator(operator);
        record.setCreateTime(LocalDateTime.now());
        inventoryRecordService.addRecord(record);

        return true;
    }

    @Override
    @Transactional
    public boolean reduceStock(Long materialId, Integer quantity, String operator) {
        return reduceStock(materialId, quantity, operator, null);
    }

    @Override
    @Transactional
    public boolean reduceStock(Long materialId, Integer quantity, String operator, String locationCode) {
        if (materialId == null || quantity == null || quantity <= 0) {
            return false;
        }

        Inventory inventory = getInventoryByMaterialId(materialId);
        if (inventory == null) {
            return false;
        }

        int currentQuantity = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        if (currentQuantity < quantity) {
            return false;
        }

        inventory.setQuantity(currentQuantity - quantity);
        BigDecimal price = inventory.getPrice() == null ? BigDecimal.ZERO : inventory.getPrice();
        inventory.setAmount(price.multiply(BigDecimal.valueOf(inventory.getQuantity())));
        inventory.setUpdateTime(LocalDateTime.now());
        updateById(inventory);

        String targetLocation = normalizeLocation(locationCode);
        if (targetLocation == null) {
            targetLocation = normalizeLocation(inventory.getLocation());
        }
        if (targetLocation != null) {
            assertLocationChange(warehouseLocationService.decreaseLoad(targetLocation, quantity), "库位载荷扣减失败");
        }

        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setMaterialId(materialId);
        record.setMaterialCode(inventory.getMaterialCode());
        record.setMaterialName(inventory.getMaterialName());
        record.setType("出库");
        record.setQuantity(quantity);
        BigDecimal recordPrice = inventory.getPrice() == null ? BigDecimal.ZERO : inventory.getPrice();
        record.setPrice(recordPrice);
        record.setAmount(recordPrice.multiply(BigDecimal.valueOf(quantity)));
        record.setOperator(operator);
        record.setCreateTime(LocalDateTime.now());
        inventoryRecordService.addRecord(record);

        return true;
    }

    @Override
    public boolean checkStock(Long materialId, Integer quantity) {
        Inventory inventory = getInventoryByMaterialId(materialId);
        if (inventory == null) {
            return false;
        }
        return inventory.getQuantity() >= quantity;
    }

    private String normalizeLocation(String locationCode) {
        if (locationCode == null) {
            return null;
        }
        String trimmed = locationCode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void assertLocationChange(boolean success, String errorMessage) {
        if (!success) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void syncMaterialSnapshot(Inventory inventory) {
        if (inventory == null || inventory.getMaterialId() == null) {
            return;
        }

        com.wms.modules.material.entity.Material material = materialService.getById(inventory.getMaterialId());
        if (material == null) {
            return;
        }

        boolean changed = false;

        if (!Objects.equals(inventory.getMaterialCode(), material.getCode())) {
            inventory.setMaterialCode(material.getCode());
            changed = true;
        }
        if (!Objects.equals(inventory.getMaterialName(), material.getName())) {
            inventory.setMaterialName(material.getName());
            changed = true;
        }
        if (!Objects.equals(inventory.getSpec(), material.getSpec())) {
            inventory.setSpec(material.getSpec());
            changed = true;
        }
        if (!Objects.equals(inventory.getUnit(), material.getUnit())) {
            inventory.setUnit(material.getUnit());
            changed = true;
        }
        if (material.getPrice() != null && !Objects.equals(inventory.getPrice(), material.getPrice())) {
            inventory.setPrice(material.getPrice());
            int quantity = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            inventory.setAmount(material.getPrice().multiply(BigDecimal.valueOf(quantity)));
            changed = true;
        }

        if (changed) {
            inventory.setUpdateTime(LocalDateTime.now());
            updateById(inventory);
        }
    }
}
