package com.wms.modules.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wms.modules.inventory.entity.Inventory;
import com.wms.modules.inventory.mapper.InventoryMapper;
import com.wms.modules.material.entity.Material;
import com.wms.modules.material.mapper.MaterialMapper;
import com.wms.modules.outbound.entity.OutboundDetail;
import com.wms.modules.outbound.mapper.OutboundDetailMapper;
import com.wms.modules.report.entity.Report;
import com.wms.modules.report.service.ReportService;
import com.wms.modules.storage.entity.Storage;
import com.wms.modules.storage.mapper.StorageMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 报表统计服务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {
    
    @Resource
    private InventoryMapper inventoryMapper;
    
    @Resource
    private MaterialMapper materialMapper;
    
    @Resource
    private StorageMapper storageMapper;
    
    @Resource
    private OutboundDetailMapper outboundDetailMapper;
    
    @Override
    public List<Report.InventoryReport> getInventoryReport() {
        List<Report.InventoryReport> reports = new ArrayList<>();
        
        // 查询所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);
        
        for (Inventory inventory : inventories) {
            Report.InventoryReport report = new Report.InventoryReport();
            report.setMaterialId(String.valueOf(inventory.getMaterialId()));
            report.setCurrentStock(inventory.getQuantity());
            report.setUpdateTime(java.util.Date.from(inventory.getUpdateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            
            // 直接使用库存记录中的物料信息
            report.setMaterialName(inventory.getMaterialName());
            report.setMaterialCode(inventory.getMaterialCode());
            report.setSpec(inventory.getSpec());
            report.setUnit(inventory.getUnit());
            report.setSafeStock(inventory.getMinStock());
            
            reports.add(report);
        }
        
        return reports;
    }
    
    @Override
    public List<Report.StorageReport> getStorageReport(Date startDate, Date endDate) {
        List<Report.StorageReport> reports = new ArrayList<>();
        
        // 构建查询条件
        QueryWrapper<Storage> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("create_time", endDate);
        }
        
        // 查询入库记录
        List<Storage> storageList = storageMapper.selectList(queryWrapper);
        
        for (Storage storage : storageList) {
            Report.StorageReport report = new Report.StorageReport();
            report.setMaterialId(String.valueOf(storage.getMaterialId()));
            report.setQuantity(storage.getQuantity());
            report.setStorageTime(java.util.Date.from(storage.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            
            // 直接使用入库记录中的物料信息
            report.setMaterialName(storage.getMaterialName());
            
            // 查询物料信息获取颜色
            Material material = materialMapper.selectById(storage.getMaterialId());
            if (material != null) {
                report.setSpec(material.getSpec());
            }
            
            reports.add(report);
        }
        
        return reports;
    }
    
    @Override
    public List<Report.OutboundReport> getOutboundReport(Date startDate, Date endDate) {
        List<Report.OutboundReport> reports = new ArrayList<>();
        
        // 构建查询条件
        QueryWrapper<OutboundDetail> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("create_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("create_time", endDate);
        }
        
        // 查询出库明细
        List<OutboundDetail> outboundDetails = outboundDetailMapper.selectList(queryWrapper);
        
        for (OutboundDetail detail : outboundDetails) {
            Report.OutboundReport report = new Report.OutboundReport();
            report.setMaterialId(String.valueOf(detail.getMaterialId()));
            report.setQuantity(detail.getQuantity());
            report.setOutboundTime(java.util.Date.from(detail.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            
            // 直接使用出库明细中的物料信息
            report.setMaterialName(detail.getMaterialName());
            
            // 直接使用出库明细中的颜色信息
            report.setSpec(detail.getSpec());
            
            reports.add(report);
        }
        
        return reports;
    }
    
    @Override
    public List<Report.InventoryTurnoverReport> getInventoryTurnoverReport(Date startDate, Date endDate) {
        List<Report.InventoryTurnoverReport> reports = new ArrayList<>();
        
        // 如果没有指定日期范围，默认使用最近90天
        if (startDate == null || endDate == null) {
            endDate = new Date();
            startDate = new Date(endDate.getTime() - 90L * 24 * 60 * 60 * 1000); // 90天前
        }
        
        // 计算日期范围的天数
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
        if (daysBetween <= 0) {
            daysBetween = 1; // 至少1天，避免除零错误
        }
        
        // 查询所有库存记录
        List<Inventory> inventories = inventoryMapper.selectList(null);
        
        for (Inventory inventory : inventories) {
            Report.InventoryTurnoverReport report = new Report.InventoryTurnoverReport();
            report.setMaterialId(String.valueOf(inventory.getMaterialId()));
            
            // 直接使用库存记录中的物料信息
            report.setMaterialName(inventory.getMaterialName());
            report.setSpec(inventory.getSpec());
            
            // 获取当前库存数量
            Integer currentStock = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
            
            // 查询该物料在日期范围内的出库总数量
            QueryWrapper<OutboundDetail> outboundQuery = new QueryWrapper<>();
            outboundQuery.eq("material_id", inventory.getMaterialId());
            outboundQuery.ge("create_time", startDate);
            outboundQuery.le("create_time", endDate);
            List<OutboundDetail> outboundDetails = outboundDetailMapper.selectList(outboundQuery);
            
            int totalOutbound = 0;
            for (OutboundDetail detail : outboundDetails) {
                totalOutbound += detail.getQuantity() != null ? detail.getQuantity() : 0;
            }
            
            // 查询该物料在日期范围内的入库总数量
            QueryWrapper<Storage> storageQuery = new QueryWrapper<>();
            storageQuery.eq("material_id", inventory.getMaterialId());
            storageQuery.ge("create_time", startDate);
            storageQuery.le("create_time", endDate);
            List<Storage> storageList = storageMapper.selectList(storageQuery);
            
            int totalInbound = 0;
            for (Storage storage : storageList) {
                totalInbound += storage.getQuantity() != null ? storage.getQuantity() : 0;
            }
            
            // 计算平均库存 = (期初库存 + 期末库存) / 2
            // 期初库存 = 当前库存 - 入库总量 + 出库总量
            int beginningInventory = currentStock - totalInbound + totalOutbound;
            int averageInventory = (beginningInventory + currentStock) / 2;
            if (averageInventory <= 0) {
                averageInventory = 1; // 避免除零错误
            }
            
            // 计算库存周转率 = 出库总数量 / 平均库存数量
            double turnoverRate = (double) totalOutbound / averageInventory;
            report.setTurnoverRate(Math.round(turnoverRate * 100.0) / 100.0); // 保留2位小数
            
            // 计算库存天数 = 平均库存数量 / 日均出库数量
            // 日均出库数量 = 出库总数量 / 日期范围天数
            double dailyOutbound = (double) totalOutbound / daysBetween;
            int daysInStock;
            if (dailyOutbound > 0) {
                daysInStock = (int) Math.round(averageInventory / dailyOutbound);
            } else {
                // 如果没有出库记录，根据库存量和最近入库时间估算
                daysInStock = currentStock > 0 ? (int) daysBetween : 0;
            }
            report.setDaysInStock(daysInStock);
            
            // 判断库存状态
            // 周转率 < 1 或 库存天数 > 90天 视为呆滞
            if (turnoverRate < 1.0 || daysInStock > 90) {
                report.setStatus("呆滞");
            } else {
                report.setStatus("正常");
            }
            
            reports.add(report);
        }
        
        return reports;
    }
}