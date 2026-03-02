package com.wms.modules.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.material.service.MaterialService;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.entity.InventoryRecord;
import com.wms.modules.inventory.mapper.InventoryMapper;
import com.wms.modules.inventory.service.InventoryRecordService;
import com.wms.modules.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private InventoryRecordService inventoryRecordService;
    
    @Autowired
    private MaterialService materialService;

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
        
        return list(wrapper);
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
        Inventory inventory = getInventoryByMaterialId(materialId);
        if (inventory == null) {
            // 从物料表获取物料信息，创建新的库存记录
            com.wms.modules.material.entity.Material material = materialService.getById(materialId);
            if (material != null) {
                inventory = new Inventory();
                inventory.setMaterialId(materialId);
                inventory.setMaterialCode(material.getCode());
                inventory.setMaterialName(material.getName());
                inventory.setSpec(material.getSpec());
                inventory.setUnit(material.getUnit());
                inventory.setQuantity(quantity);
                inventory.setPrice(material.getPrice() != null ? material.getPrice() : BigDecimal.ZERO);
                inventory.setAmount(inventory.getPrice().multiply(BigDecimal.valueOf(quantity)));
                inventory.setLocation("");
                inventory.setMinStock(material.getMinStock() != null ? material.getMinStock() : 0);
                inventory.setMaxStock(material.getMaxStock() != null ? material.getMaxStock() : 0);
                inventory.setStatus(1);
                inventory.setCreateTime(LocalDateTime.now());
                inventory.setUpdateTime(LocalDateTime.now());
                save(inventory);
            } else {
                return false;
            }
        } else {
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.setUpdateTime(LocalDateTime.now());
            updateById(inventory);
        }
        
        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setMaterialId(materialId);
        record.setMaterialCode(inventory.getMaterialCode());
        record.setMaterialName(inventory.getMaterialName());
        record.setType("入库");
        record.setQuantity(quantity);
        record.setPrice(inventory.getPrice());
        record.setAmount(inventory.getPrice().multiply(BigDecimal.valueOf(quantity)));
        record.setOperator(operator);
        record.setCreateTime(LocalDateTime.now());
        inventoryRecordService.addRecord(record);
        
        return true;
    }

    @Override
    @Transactional
    public boolean reduceStock(Long materialId, Integer quantity, String operator) {
        Inventory inventory = getInventoryByMaterialId(materialId);
        if (inventory == null) {
            return false;
        }
        
        if (inventory.getQuantity() < quantity) {
            return false;
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setUpdateTime(LocalDateTime.now());
        updateById(inventory);
        
        InventoryRecord record = new InventoryRecord();
        record.setInventoryId(inventory.getId());
        record.setMaterialId(materialId);
        record.setMaterialCode(inventory.getMaterialCode());
        record.setMaterialName(inventory.getMaterialName());
        record.setType("出库");
        record.setQuantity(quantity);
        record.setPrice(inventory.getPrice());
        record.setAmount(inventory.getPrice().multiply(BigDecimal.valueOf(quantity)));
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
}
