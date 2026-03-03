package com.wms.modules.location.controller;

import com.wms.common.result.Result;
import com.wms.modules.location.dto.LocationPolicyEvaluationResponse;
import com.wms.modules.location.dto.LocationPolicyModeRequest;
import com.wms.modules.location.dto.LocationPolicyReloadRequest;
import com.wms.modules.location.dto.LocationPolicyStatusResponse;
import com.wms.modules.location.dto.LocationRecommendationRequest;
import com.wms.modules.location.dto.LocationRecommendationResponse;
import com.wms.modules.location.entity.WarehouseLocation;
import com.wms.modules.location.service.LocationAllocationService;
import com.wms.modules.location.service.WarehouseLocationService;
import com.wms.modules.material.entity.Material;
import com.wms.modules.material.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private WarehouseLocationService warehouseLocationService;

    @Autowired
    private LocationAllocationService locationAllocationService;

    @Autowired
    private MaterialService materialService;

    @GetMapping("/list")
    public Result<List<WarehouseLocation>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            List<WarehouseLocation> locations = warehouseLocationService.listLocations(keyword, status);
            return Result.success("获取成功", locations);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<WarehouseLocation> add(@RequestBody WarehouseLocation location) {
        try {
            boolean success = warehouseLocationService.saveLocation(location);
            if (success) {
                return Result.success("新增成功", location);
            }
            return Result.error("新增失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<WarehouseLocation> update(@RequestBody WarehouseLocation location) {
        try {
            if (location.getId() == null) {
                return Result.error("库位ID不能为空");
            }
            boolean success = warehouseLocationService.updateLocation(location);
            if (success) {
                return Result.success("修改成功", location);
            }
            return Result.error("修改失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean success = warehouseLocationService.removeLocation(id);
            if (success) {
                return Result.success("删除成功");
            }
            return Result.error("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/recommend")
    public Result<LocationRecommendationResponse> recommend(@RequestBody LocationRecommendationRequest request) {
        try {
            LocationRecommendationResponse response = locationAllocationService.recommend(request);
            if (response == null) {
                return Result.error("暂无可推荐库位");
            }
            return Result.success("推荐成功", response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @PostMapping("/allocate")
    public Result<LocationRecommendationResponse> allocate(@RequestBody LocationRecommendationRequest request) {
        try {
            LocationRecommendationResponse response = locationAllocationService.recommend(request);
            if (response == null) {
                return Result.error("分配失败：暂无可用库位");
            }

            Material material = request.getMaterialId() == null ? null : materialService.getById(request.getMaterialId());
            locationAllocationService.recordAllocation(
                    request.getMaterialId(),
                    material == null ? null : material.getCode(),
                    material == null ? null : material.getName(),
                    request.getInboundQuantity(),
                    "manual",
                    request,
                    response
            );

            return Result.success("分配成功", response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/policy/status")
    public Result<LocationPolicyStatusResponse> policyStatus() {
        try {
            return Result.success("获取成功", locationAllocationService.getPolicyStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取策略状态失败：" + e.getMessage());
        }
    }

    @PostMapping("/policy/reload")
    public Result<String> reloadPolicy(@RequestBody(required = false) LocationPolicyReloadRequest request) {
        try {
            String modelPath = request == null ? null : request.getModelPath();
            String modelVersion = request == null ? null : request.getModelVersion();
            boolean success = locationAllocationService.reloadPolicyModel(modelPath, modelVersion, "admin");
            return success ? Result.success("模型重载成功") : Result.error("模型重载失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("模型重载失败：" + e.getMessage());
        }
    }

    @PutMapping("/policy/mode")
    public Result<String> updateMode(@RequestBody LocationPolicyModeRequest request) {
        try {
            if (request == null || request.getMode() == null || request.getMode().trim().isEmpty()) {
                return Result.error("mode不能为空");
            }
            locationAllocationService.updatePolicyMode(request.getMode(), request.getOperator());
            return Result.success("策略模式更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("策略模式更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/policy/evaluation")
    public Result<LocationPolicyEvaluationResponse> evaluation(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            LocalDateTime fromTime = parseDateTime(from);
            LocalDateTime toTime = parseDateTime(to);
            return Result.success("获取成功", locationAllocationService.evaluatePolicy(fromTime, toTime));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("策略评估获取失败：" + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.contains("T")) {
                return LocalDateTime.parse(text);
            }
            return LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
            return null;
        }
    }
}
