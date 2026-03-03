package com.wms.modules.location.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.location.entity.WarehouseLocation;
import com.wms.modules.location.mapper.WarehouseLocationMapper;
import com.wms.modules.location.service.WarehouseLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarehouseLocationServiceImpl extends ServiceImpl<WarehouseLocationMapper, WarehouseLocation> implements WarehouseLocationService {

    @Override
    public List<WarehouseLocation> listLocations(String keyword, Integer status) {
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like("code", keyword).or().like("zone", keyword));
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByAsc("zone").orderByAsc("code");
        return list(wrapper);
    }

    @Override
    public WarehouseLocation getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        QueryWrapper<WarehouseLocation> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code.trim());
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean saveLocation(WarehouseLocation location) {
        if (location.getStatus() == null) {
            location.setStatus(1);
        }
        if (location.getCurrentLoad() == null) {
            location.setCurrentLoad(0);
        }
        if (location.getPriority() == null) {
            location.setPriority(0);
        }
        if (location.getCapacity() == null || location.getCapacity() < 0) {
            location.setCapacity(0);
        }
        location.setCreateTime(LocalDateTime.now());
        location.setUpdateTime(LocalDateTime.now());
        return save(location);
    }

    @Override
    public boolean updateLocation(WarehouseLocation location) {
        location.setUpdateTime(LocalDateTime.now());
        if (location.getCapacity() != null && location.getCapacity() < 0) {
            location.setCapacity(0);
        }
        return updateById(location);
    }

    @Override
    public boolean removeLocation(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean increaseLoad(String code, Integer quantity) {
        if (code == null || code.trim().isEmpty() || quantity == null || quantity <= 0) {
            return true;
        }

        WarehouseLocation location = getByCode(code);
        if (location == null || location.getStatus() == null || location.getStatus() != 1) {
            // 兼容历史数据：如果库位主数据不存在，不阻塞入库流程
            return true;
        }

        int capacity = location.getCapacity() == null ? 0 : location.getCapacity();
        int currentLoad = location.getCurrentLoad() == null ? 0 : location.getCurrentLoad();

        if (capacity > 0 && currentLoad + quantity > capacity) {
            return false;
        }

        location.setCurrentLoad(currentLoad + quantity);
        location.setUpdateTime(LocalDateTime.now());
        return updateById(location);
    }

    @Override
    @Transactional
    public boolean decreaseLoad(String code, Integer quantity) {
        if (code == null || code.trim().isEmpty() || quantity == null || quantity <= 0) {
            return true;
        }

        WarehouseLocation location = getByCode(code);
        if (location == null) {
            return true;
        }

        int currentLoad = location.getCurrentLoad() == null ? 0 : location.getCurrentLoad();
        int newLoad = currentLoad - quantity;
        if (newLoad < 0) {
            newLoad = 0;
        }

        location.setCurrentLoad(newLoad);
        location.setUpdateTime(LocalDateTime.now());
        return updateById(location);
    }
}
