package com.wms.modules.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.service.InventoryService;
import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.dto.LocationRecommendationResponse;
import com.wms.modules.location.service.LocationAllocationService;
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

    @Autowired
    private LocationAllocationService locationAllocationService;

    @Override
    public List<Storage> listStorages(String keyword, Long materialId, String batchNumber) {
        QueryWrapper<Storage> wrapper = new QueryWrapper<Storage>();

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

        String policyMode = locationAllocationService.getCurrentPolicyMode();

        LocationRecommendationRequest recommendationRequest = null;
        LocationRecommendationResponse recommendationResponse = null;

        if (isBlank(storage.getLocation())
                && storage.getMaterialId() != null
                && storage.getQuantity() != null
                && storage.getQuantity() > 0) {

            if ("manual".equalsIgnoreCase(policyMode)) {
                throw new IllegalStateException("当前库位策略为 manual，请先获取RL建议并人工确认库位后再提交");
            }

            Inventory inventory = inventoryService.getInventoryByMaterialId(storage.getMaterialId());

            recommendationRequest = new LocationRecommendationRequest();
            recommendationRequest.setMaterialId(storage.getMaterialId());
            recommendationRequest.setInboundQuantity(storage.getQuantity());
            recommendationRequest.setPreferredLocation(inventory == null ? null : inventory.getLocation());

            recommendationResponse = locationAllocationService.recommend(recommendationRequest);
            if (recommendationResponse != null && !isBlank(recommendationResponse.getLocationCode())) {
                storage.setLocation(recommendationResponse.getLocationCode());
                storage.setPolicyTraceId(recommendationResponse.getTraceId());
            }
        }

        boolean result = save(storage);

        if (result && storage.getMaterialId() != null && storage.getQuantity() != null) {
            boolean stockUpdated = inventoryService.addStock(
                    storage.getMaterialId(),
                    storage.getQuantity(),
                    storage.getOperator(),
                    storage.getLocation()
            );
            if (!stockUpdated) {
                throw new IllegalStateException("入库后更新库存失败");
            }
        }

        if (result && recommendationResponse != null) {
            locationAllocationService.recordAllocation(
                    storage.getMaterialId(),
                    storage.getMaterialCode(),
                    storage.getMaterialName(),
                    storage.getQuantity(),
                    storage.getOperator(),
                    recommendationRequest,
                    recommendationResponse
            );
        } else if (result && !isBlank(storage.getPolicyTraceId())) {
            locationAllocationService.completePolicyEvent(
                    storage.getPolicyTraceId(),
                    storage.getLocation(),
                    storage.getOperator()
            );
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
            boolean stockReduced = inventoryService.reduceStock(
                    storage.getMaterialId(),
                    storage.getQuantity(),
                    storage.getOperator(),
                    storage.getLocation()
            );
            if (!stockReduced) {
                throw new IllegalStateException("删除入库记录后回退库存失败");
            }
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
