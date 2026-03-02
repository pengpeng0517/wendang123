package com.wms.modules.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.entity.InventoryAlert;
import com.wms.modules.inventory.mapper.InventoryAlertMapper;
import com.wms.modules.inventory.service.InventoryAlertService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryAlertServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert> implements InventoryAlertService {

    @Override
    public List<InventoryAlert> listAlerts(Integer status) {
        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(InventoryAlert::getStatus, status);
        }
        wrapper.orderByDesc(InventoryAlert::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<InventoryAlert> listAlertsByMaterialId(Long materialId) {
        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryAlert::getMaterialId, materialId);
        wrapper.orderByDesc(InventoryAlert::getCreateTime);
        return list(wrapper);
    }

    @Override
    public boolean createAlert(InventoryAlert alert) {
        alert.setCreateTime(LocalDateTime.now());
        alert.setUpdateTime(LocalDateTime.now());
        return save(alert);
    }

    @Override
    public boolean updateAlertStatus(Long id, Integer status) {
        InventoryAlert alert = getById(id);
        if (alert == null) {
            return false;
        }
        alert.setStatus(status);
        alert.setUpdateTime(LocalDateTime.now());
        return updateById(alert);
    }
}
