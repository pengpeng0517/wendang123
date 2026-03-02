package com.wms.modules.report.service;

import com.wms.modules.report.entity.Report;

import java.util.Date;
import java.util.List;

/**
 * 报表统计服务接口
 */
public interface ReportService {
    
    /**
     * 获取库存报表
     * @return 库存报表数据
     */
    List<Report.InventoryReport> getInventoryReport();
    
    /**
     * 获取入库统计
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 入库统计数据
     */
    List<Report.StorageReport> getStorageReport(Date startDate, Date endDate);
    
    /**
     * 获取出库统计
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 出库统计数据
     */
    List<Report.OutboundReport> getOutboundReport(Date startDate, Date endDate);
    
    /**
     * 获取库存周转分析
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 库存周转分析数据
     */
    List<Report.InventoryTurnoverReport> getInventoryTurnoverReport(Date startDate, Date endDate);
}