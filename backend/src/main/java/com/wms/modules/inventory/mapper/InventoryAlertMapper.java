package com.wms.modules.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.modules.inventory.entity.InventoryAlert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InventoryAlertMapper extends BaseMapper<InventoryAlert> {

    List<InventoryAlert> listAlerts(Integer status);

    List<InventoryAlert> listAlertsByMaterialId(Long materialId);
}
