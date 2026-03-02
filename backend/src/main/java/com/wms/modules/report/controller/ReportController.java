package com.wms.modules.report.controller;

import com.wms.common.result.Result;
import com.wms.modules.report.entity.Report;
import com.wms.modules.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 报表统计控制器
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {
    
    @Resource
    private ReportService reportService;
    
    /**
     * 获取库存报表
     * @return 库存报表数据
     */
    @GetMapping("/inventory")
    public Result<List<Report.InventoryReport>> getInventoryReport() {
        List<Report.InventoryReport> reports = reportService.getInventoryReport();
        return Result.success("获取库存报表成功", reports);
    }
    
    /**
     * 获取入库统计
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 入库统计数据
     */
    @GetMapping("/storage")
    public Result<List<Report.StorageReport>> getStorageReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Date start = null;
        Date end = null;
        
        try {
            if (startDate != null) {
                start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            }
            if (endDate != null) {
                end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        List<Report.StorageReport> reports = reportService.getStorageReport(start, end);
        return Result.success("获取入库统计成功", reports);
    }
    
    /**
     * 获取出库统计
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 出库统计数据
     */
    @GetMapping("/outbound")
    public Result<List<Report.OutboundReport>> getOutboundReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Date start = null;
        Date end = null;
        
        try {
            if (startDate != null) {
                start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            }
            if (endDate != null) {
                end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        List<Report.OutboundReport> reports = reportService.getOutboundReport(start, end);
        return Result.success("获取出库统计成功", reports);
    }
    
    /**
     * 获取库存周转分析
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 库存周转分析数据
     */
    @GetMapping("/turnover")
    public Result<List<Report.InventoryTurnoverReport>> getInventoryTurnoverReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Date start = null;
        Date end = null;
        
        try {
            if (startDate != null) {
                start = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            }
            if (endDate != null) {
                end = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        List<Report.InventoryTurnoverReport> reports = reportService.getInventoryTurnoverReport(start, end);
        return Result.success("获取库存周转分析成功", reports);
    }
}