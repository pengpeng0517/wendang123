package com.wms.modules.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.modules.inventory.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {

    List<InventoryRecord> listRecords(Long inventoryId);

    List<InventoryRecord> listRecordsByMaterialId(Long materialId);
}
