package com.wms.modules.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.material.entity.Material;

import java.util.List;

public interface MaterialService extends IService<Material> {

    List<Material> listMaterials(String keyword, Long categoryId, Long supplierId);

    Material getMaterialById(Long id);

    boolean saveMaterial(Material material);

    boolean updateMaterial(Material material);

    boolean removeMaterial(Long id);
}
