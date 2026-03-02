package com.wms.modules.report.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 报表统计实体类
 */
@Data
public class Report {
    
    // 库存报表数据
    private List<InventoryReport> inventoryReports;
    
    // 入库统计数据
    private List<StorageReport> storageReports;
    
    // 出库统计数据
    private List<OutboundReport> outboundReports;
    
    // 库存周转分析数据
    private List<InventoryTurnoverReport> inventoryTurnoverReports;
    
    /**
     * 库存报表数据
     */
    @Data
    public static class InventoryReport {
        private String materialId;
        private String materialName;
        private String materialCode;
        private String spec;
        private String unit;
        private Integer currentStock;
        private Integer safeStock;
        private Date updateTime;
    }
    
    /**
     * 入库统计数据
     */
    @Data
    public static class StorageReport {
        private String materialId;
        private String materialName;
        private String spec;
        private Integer quantity;
        private String supplierName;
        private Date storageTime;
    }
    
    /**
     * 出库统计数据
     */
    @Data
    public static class OutboundReport {
        private String materialId;
        private String materialName;
        private String spec;
        private Integer quantity;
        private String receiver;
        private Date outboundTime;
    }
    
    /**
     * 库存周转分析数据
     */
    @Data
    public static class InventoryTurnoverReport {
        private String materialId;
        private String materialName;
        private String spec;
        private Double turnoverRate;
        private Integer daysInStock;
        private String status; // 正常、呆滞
    }
}