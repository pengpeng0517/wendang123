package com.wms.modules.material.controller;

import com.wms.common.result.Result;
import com.wms.modules.material.entity.Material;
import com.wms.modules.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping("/list")
    public Result<List<Material>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId) {
        try {
            List<Material> materialList = materialService.listMaterials(keyword, categoryId, supplierId);
            return Result.success("获取成功", materialList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public Result<Material> info(@PathVariable Long id) {
        try {
            Material material = materialService.getMaterialById(id);
            if (material == null) {
                return Result.error("原材料不存在");
            }
            return Result.success("获取成功", material);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<Material> add(@RequestBody Material material) {
        try {
            boolean success = materialService.saveMaterial(material);
            if (success) {
                return Result.success("新增成功", material);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Material> update(@RequestBody Material material) {
        try {
            if (material.getId() == null) {
                return Result.error("原材料ID不能为空");
            }
            boolean success = materialService.updateMaterial(material);
            if (success) {
                return Result.success("修改成功", material);
            } else {
                return Result.error("修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean success = materialService.removeMaterial(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }
}
