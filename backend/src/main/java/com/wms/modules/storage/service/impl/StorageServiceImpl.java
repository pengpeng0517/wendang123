package com.wms.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.service.InventoryService;
import com.wms.modules.storage.entity.Storage;
import com.wms.modules.storage.mapper.StorageMapper;
import com.wms.modules.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StorageServiceImpl extends ServiceImpl<StorageMapper, Storage> implements StorageService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public List<Storage> listStorages(String keyword, Long materialId, String batchNumber) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("material_name", keyword).or().like("batch_number", keyword));
        }
        
        if (materialId != null) {
            wrapper.eq("material_id", materialId);
        }
        
        if (batchNumber != null && !batchNumber.isEmpty()) {
            wrapper.eq("batch_number", batchNumber);
        }
        
        wrapper.orderByAsc("id");
        
        return list(wrapper);
    }

    @Override
    public Storage getStorageById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean saveStorage(Storage storage) {
        storage.setCreateTime(LocalDateTime.now());
        storage.setUpdateTime(LocalDateTime.now());
        storage.setStatus(1);
        storage.setStorageTime(LocalDateTime.now());
        
        boolean result = save(storage);
        
        if (result && storage.getMaterialId() != null && storage.getQuantity() != null) {
            inventoryService.addStock(storage.getMaterialId(), storage.getQuantity(), storage.getOperator());
        }
        
        return result;
    }

    @Override
    public boolean updateStorage(Storage storage) {
        storage.setUpdateTime(LocalDateTime.now());
        return updateById(storage);
    }

    @Override
    @Transactional
    public boolean removeStorage(Long id) {
        Storage storage = getById(id);
        boolean result = removeById(id);
        
        if (result && storage != null && storage.getMaterialId() != null && storage.getQuantity() != null) {
            inventoryService.reduceStock(storage.getMaterialId(), storage.getQuantity(), storage.getOperator());
        }
        
        return result;
    }

}
