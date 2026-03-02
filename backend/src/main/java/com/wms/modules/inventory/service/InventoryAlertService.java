package com.wms.modules.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.inventory.entity.InventoryAlert;

import java.util.List;

public interface InventoryAlertService extends IService<InventoryAlert> {

    List<InventoryAlert> listAlerts(Integer status);

    List<InventoryAlert> listAlertsByMaterialId(Long materialId);

    boolean createAlert(InventoryAlert alert);

    boolean updateAlertStatus(Long id, Integer status);
}
