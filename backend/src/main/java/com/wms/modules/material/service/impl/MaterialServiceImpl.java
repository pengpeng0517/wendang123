package com.wms.modules.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.service.InventoryService;
import com.wms.modules.material.entity.Material;
import com.wms.modules.material.mapper.MaterialMapper;
import com.wms.modules.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements MaterialService {

    @Autowired
    private InventoryService inventoryService;

    @Override
    public List<Material> listMaterials(String keyword, Long categoryId, Long supplierId) {
        QueryWrapper<Material> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("code", keyword).or().like("spec", keyword));
        }
        
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        
        if (supplierId != null) {
            wrapper.eq("supplier_id", supplierId);
        }
        
        wrapper.orderByAsc("code");
        
        return list(wrapper);
    }

    @Override
    public Material getMaterialById(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public boolean saveMaterial(Material material) {
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        material.setStatus(1);
        
        if (material.getCode() == null || material.getCode().isEmpty()) {
            material.setCode(generateMaterialCode());
        }
        
        boolean result = save(material);
        
        if (result) {
            Inventory inventory = new Inventory();
            inventory.setMaterialId(material.getId());
            inventory.setMaterialCode(material.getCode());
            inventory.setMaterialName(material.getName());
            inventory.setSpec(material.getSpec());
            inventory.setUnit(material.getUnit());
            inventory.setQuantity(0);
            inventory.setPrice(material.getPrice() != null ? material.getPrice() : BigDecimal.ZERO);
            inventory.setAmount(BigDecimal.ZERO);
            inventory.setLocation("");
            inventory.setMinStock(0);
            inventory.setMaxStock(0);
            inventory.setStatus(1);
            inventory.setCreateTime(LocalDateTime.now());
            inventory.setUpdateTime(LocalDateTime.now());
            inventoryService.save(inventory);
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateMaterial(Material material) {
        material.setUpdateTime(LocalDateTime.now());
        boolean updated = updateById(material);
        if (!updated || material.getId() == null) {
            return updated;
        }

        // 保持库存镜像字段与物料主数据同步，避免颜色等属性显示不同步
        Inventory inventory = inventoryService.getInventoryByMaterialId(material.getId());
        if (inventory != null) {
            inventory.setMaterialCode(material.getCode());
            inventory.setMaterialName(material.getName());
            inventory.setSpec(material.getSpec());
            inventory.setUnit(material.getUnit());
            if (material.getPrice() != null) {
                inventory.setPrice(material.getPrice());
                int quantity = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
                inventory.setAmount(material.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }
            inventory.setUpdateTime(LocalDateTime.now());
            inventoryService.updateById(inventory);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean removeMaterial(Long id) {
        Material material = getById(id);
        if (material == null) {
            return false;
        }
        
        boolean result = removeById(id);
        
        if (result) {
            Inventory inventory = inventoryService.getInventoryByMaterialId(id);
            if (inventory != null) {
                inventoryService.removeById(inventory.getId());
            }
        }
        
        return result;
    }

    private String generateMaterialCode() {
        // 查询最大的物料编码
        QueryWrapper<Material> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("code").last("LIMIT 1");
        Material material = getOne(wrapper);
        
        if (material == null) {
            return "MAT000001";
        }
        
        String maxCode = material.getCode();
        try {
            // 提取数字部分并加1
            int number = Integer.parseInt(maxCode.substring(3));
            return "MAT" + String.format("%06d", number + 1);
        } catch (NumberFormatException e) {
            // 如果编码格式异常，使用count()作为 fallback
            long count = count();
            return "MAT" + String.format("%06d", count + 1);
        }
    }
}
