package com.wms.modules.location.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.location.entity.WarehouseLocation;

import java.util.List;

public interface WarehouseLocationService extends IService<WarehouseLocation> {

    List<WarehouseLocation> listLocations(String keyword, Integer status);

    WarehouseLocation getByCode(String code);

    boolean saveLocation(WarehouseLocation location);

    boolean updateLocation(WarehouseLocation location);

    boolean removeLocation(Long id);

    boolean increaseLoad(String code, Integer quantity);

    boolean decreaseLoad(String code, Integer quantity);
}
