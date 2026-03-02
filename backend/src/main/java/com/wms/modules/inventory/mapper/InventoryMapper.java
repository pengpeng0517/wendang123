package com.wms.modules.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.modules.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    List<Inventory> listInventory(String keyword, Long materialId);

    Inventory getInventoryByMaterialId(Long materialId);
}
