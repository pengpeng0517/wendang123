package com.wms.modules.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.inventory.entity.InventoryRecord;

import java.util.List;

public interface InventoryRecordService extends IService<InventoryRecord> {

    List<InventoryRecord> listRecords(Long inventoryId);

    List<InventoryRecord> listRecordsByMaterialId(Long materialId);

    boolean addRecord(InventoryRecord record);
}
