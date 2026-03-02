package com.wms.modules.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.entity.InventoryRecord;
import com.wms.modules.inventory.mapper.InventoryRecordMapper;
import com.wms.modules.inventory.service.InventoryRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryRecordServiceImpl extends ServiceImpl<InventoryRecordMapper, InventoryRecord> implements InventoryRecordService {

    @Override
    public List<InventoryRecord> listRecords(Long inventoryId) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getInventoryId, inventoryId);
        wrapper.orderByDesc(InventoryRecord::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<InventoryRecord> listRecordsByMaterialId(Long materialId) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryRecord::getMaterialId, materialId);
        wrapper.orderByDesc(InventoryRecord::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean addRecord(InventoryRecord record) {
        return save(record);
    }
}
