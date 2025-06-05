package com.fivebear.shipment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 发货单实体类
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Data
@TableName("shipment")
public class Shipment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发货单号
     */
    private String shipmentNumber;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 发货状态: pending-待发货, shipped-已发货, delivered-已签收, cancelled-已取消
     */
    private String status;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 重量(kg)
     */
    private BigDecimal weight;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 备注
     */
    private String notes;

    /**
     * 发货时间
     */
    private LocalDateTime shippedAt;

    /**
     * 签收时间
     */
    private LocalDateTime deliveredAt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // Manual setter methods
    public void setId(Long id) {
        this.id = id;
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public void setLogisticsCompany(String logisticsCompany) {
        this.logisticsCompany = logisticsCompany;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // Manual getter methods
    public Long getId() { return id; }
    public String getShipmentNumber() { return shipmentNumber; }
    public Long getOrderId() { return orderId; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getStatus() { return status; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getWeight() { return weight; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public String getNotes() { return notes; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
} 