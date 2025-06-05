package com.fivebear.report.controller;

import com.fivebear.common.result.Result;
import com.fivebear.report.entity.ReportData;
import com.fivebear.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表管理控制器
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Tag(name = "报表管理", description = "数据报表和统计分析接口")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "获取销售报表", description = "获取指定时间范围内的销售数据报表")
    @GetMapping("/sales")
    public Result<List<ReportData>> getSalesReport(
            @Parameter(description = "开始日期", example = "2024-01-01") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-01-31") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "报表类型", example = "daily") 
            @RequestParam(defaultValue = "daily") String type) {
        
        List<ReportData> salesData = reportService.getSalesReport(startDate, endDate, type);
        return Result.ok(salesData);
    }

    @Operation(summary = "获取收入报表", description = "获取收入统计报表")
    @GetMapping("/revenue")
    public Result<Map<String, Object>> getRevenueReport(
            @Parameter(description = "开始日期", example = "2024-01-01") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-01-31") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Map<String, Object> revenueData = reportService.getRevenueReport(startDate, endDate);
        return Result.ok(revenueData);
    }

    @Operation(summary = "获取产品销量排行", description = "获取产品销量排行榜")
    @GetMapping("/product-ranking")
    public Result<List<Map<String, Object>>> getProductRanking(
            @Parameter(description = "开始日期", example = "2024-01-01") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-01-31") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "排行数量", example = "10") 
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<Map<String, Object>> productRanking = reportService.getProductRanking(startDate, endDate, limit);
        return Result.ok(productRanking);
    }

    @Operation(summary = "获取客户分析报表", description = "获取客户相关统计分析")
    @GetMapping("/customer-analysis")
    public Result<Map<String, Object>> getCustomerAnalysis(
            @Parameter(description = "开始日期", example = "2024-01-01") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-01-31") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Map<String, Object> customerAnalysis = reportService.getCustomerAnalysis(startDate, endDate);
        return Result.ok(customerAnalysis);
    }

    @Operation(summary = "获取库存报表", description = "获取当前库存状态报表")
    @GetMapping("/inventory")
    public Result<List<Map<String, Object>>> getInventoryReport() {
        List<Map<String, Object>> inventoryData = reportService.getInventoryReport();
        return Result.ok(inventoryData);
    }

    @Operation(summary = "获取财务概览", description = "获取财务数据概览")
    @GetMapping("/financial-overview")
    public Result<Map<String, Object>> getFinancialOverview(
            @Parameter(description = "年份", example = "2024") 
            @RequestParam(defaultValue = "2024") Integer year,
            @Parameter(description = "月份", example = "1") 
            @RequestParam(required = false) Integer month) {
        
        Map<String, Object> financialOverview = reportService.getFinancialOverview(year, month);
        return Result.ok(financialOverview);
    }

    @Operation(summary = "导出报表", description = "导出指定类型的报表文件")
    @PostMapping("/export")
    public Result<String> exportReport(
            @Parameter(description = "报表类型", example = "sales") 
            @RequestParam String reportType,
            @Parameter(description = "开始日期", example = "2024-01-01") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2024-01-31") 
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "导出格式", example = "excel") 
            @RequestParam(defaultValue = "excel") String format) {
        
        String exportPath = reportService.exportReport(reportType, startDate, endDate, format);
        return Result.ok(exportPath, "报表导出成功");
    }

    @Operation(summary = "获取实时统计", description = "获取实时业务统计数据")
    @GetMapping("/realtime-stats")
    public Result<Map<String, Object>> getRealtimeStats() {
        Map<String, Object> realtimeStats = reportService.getRealtimeStats();
        return Result.ok(realtimeStats);
    }

    @Operation(summary = "获取趋势分析", description = "获取业务趋势分析数据")
    @GetMapping("/trend-analysis")
    public Result<Map<String, Object>> getTrendAnalysis(
            @Parameter(description = "分析类型", example = "sales") 
            @RequestParam String analysisType,
            @Parameter(description = "时间周期", example = "30") 
            @RequestParam(defaultValue = "30") Integer days) {
        
        Map<String, Object> trendAnalysis = reportService.getTrendAnalysis(analysisType, days);
        return Result.ok(trendAnalysis);
    }
} 