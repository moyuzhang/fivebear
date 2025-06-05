package com.fivebear.report.service;

import com.fivebear.report.entity.ReportData;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
public interface ReportService {

    /**
     * 获取销售报表
     */
    List<ReportData> getSalesReport(LocalDate startDate, LocalDate endDate, String type);

    /**
     * 获取收入报表
     */
    Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate);

    /**
     * 获取产品销量排行
     */
    List<Map<String, Object>> getProductRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取客户分析
     */
    Map<String, Object> getCustomerAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * 获取库存报表
     */
    List<Map<String, Object>> getInventoryReport();

    /**
     * 获取财务概览
     */
    Map<String, Object> getFinancialOverview(Integer year, Integer month);

    /**
     * 导出报表
     */
    String exportReport(String reportType, LocalDate startDate, LocalDate endDate, String format);

    /**
     * 获取实时统计
     */
    Map<String, Object> getRealtimeStats();

    /**
     * 获取趋势分析
     */
    Map<String, Object> getTrendAnalysis(String analysisType, Integer days);
} 