package com.fivebear.shipment.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fivebear.shipment.entity.Shipment;

/**
 * 发货服务接口
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
public interface ShipmentService extends IService<Shipment> {

    /**
     * 确认发货
     */
    boolean confirmShipment(Long shipmentId, String logisticsCompany, String trackingNumber);

    /**
     * 取消发货
     */
    boolean cancelShipment(Long shipmentId, String reason);

    /**
     * 更新物流状态
     */
    boolean updateTrackingStatus(Long shipmentId, String status);

    /**
     * 批量发货
     */
    Map<String, Object> batchShipment(List<Long> shipmentIds, String logisticsCompany);

    /**
     * 生成发货标签
     */
    String generateShippingLabel(Long shipmentId);

    /**
     * 获取发货统计
     */
    Map<String, Object> getShipmentStatistics(String startDate, String endDate);

    /**
     * 导出发货记录
     */
    String exportShipmentRecords(String startDate, String endDate, String format);

    /**
     * 获取发货单列表
     */
    List<Shipment> getShipmentList(Integer page, Integer size, String keyword, String status, LocalDate startDate, LocalDate endDate);

    /**
     * 根据ID获取发货单
     */
    Shipment getShipmentById(Long id);

    /**
     * 创建发货单
     */
    Shipment createShipment(Shipment shipment);

    /**
     * 更新发货单
     */
    Shipment updateShipment(Shipment shipment);

    /**
     * 删除发货单
     */
    void deleteShipment(Long id);

    /**
     * 确认发货（重载方法）
     */
    void confirmShipment(Long id);

    /**
     * 更新物流信息
     */
    void updateLogistics(Long id, Map<String, Object> logisticsInfo);

    /**
     * 获取物流跟踪信息
     */
    List<Map<String, Object>> getShipmentTracking(Long id);

    /**
     * 批量发货（重载方法）
     */
    Map<String, Object> batchShipment(List<Long> shipmentIds);

    /**
     * 获取发货统计（重载方法）
     */
    Map<String, Object> getShipmentStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 导出发货单
     */
    String exportShipments(Map<String, Object> exportParams);
} 