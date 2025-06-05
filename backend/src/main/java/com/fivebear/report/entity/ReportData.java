package com.fivebear.report.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * 报表数据实体类
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Data
public class ReportData {

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 数值
     */
    private BigDecimal value;

    /**
     * 标签
     */
    private String label;

    /**
     * 分类
     */
    private String category;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 百分比
     */
    private Double percentage;

    /**
     * 额外数据
     */
    private Object extraData;

    public ReportData setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public ReportData setValue(BigDecimal value) {
        this.value = value;
        return this;
    }

    public ReportData setLabel(String label) {
        this.label = label;
        return this;
    }

    public ReportData setCategory(String category) {
        this.category = category;
        return this;
    }

    public ReportData setCount(Integer count) {
        this.count = count;
        return this;
    }

    // Manual getter methods
    public LocalDate getDate() { return date; }
    public BigDecimal getValue() { return value; }
    public String getLabel() { return label; }
    public String getCategory() { return category; }
    public Integer getCount() { return count; }
    public Double getPercentage() { return percentage; }
    public Object getExtraData() { return extraData; }
} 