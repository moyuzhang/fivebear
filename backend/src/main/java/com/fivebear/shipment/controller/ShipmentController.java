package com.fivebear.shipment.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fivebear.common.result.Result;
import com.fivebear.shipment.entity.Shipment;
import com.fivebear.shipment.service.ShipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 出货管理控制器
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Tag(name = "出货管理", description = "货物出库和物流管理接口")
@RestController
@RequestMapping("/shipment")
@Validated
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @Operation(summary = "获取出货单列表", description = "分页查询出货单列表")
    @GetMapping("/list")
    public Result<List<Shipment>> getShipmentList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "出货状态") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        List<Shipment> shipments = shipmentService.getShipmentList(page, size, keyword, status, startDate, endDate);
        return Result.ok(shipments);
    }

    @Operation(summary = "获取出货单详情", description = "根据ID获取出货单详细信息")
    @GetMapping("/{id}")
    public Result<Shipment> getShipmentById(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id) {

        Shipment shipment = shipmentService.getShipmentById(id);
        return Result.ok(shipment);
    }

    @Operation(summary = "创建出货单", description = "创建新的出货单")
    @PostMapping
    public Result<Shipment> createShipment(@Valid @RequestBody Shipment shipment) {
        Shipment createdShipment = shipmentService.createShipment(shipment);
        return Result.ok(createdShipment, "出货单创建成功");
    }

    @Operation(summary = "更新出货单", description = "更新出货单信息")
    @PutMapping("/{id}")
    public Result<Shipment> updateShipment(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody Shipment shipment) {

        shipment.setId(id);
        Shipment updatedShipment = shipmentService.updateShipment(shipment);
        return Result.ok(updatedShipment, "出货单更新成功");
    }

    @Operation(summary = "删除出货单", description = "删除指定的出货单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteShipment(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id) {

        shipmentService.deleteShipment(id);
        return Result.ok("出货单删除成功");
    }

    @Operation(summary = "确认出货", description = "确认出货单并更新状态")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmShipment(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id) {

        shipmentService.confirmShipment(id);
        return Result.ok("出货确认成功");
    }

    @Operation(summary = "取消出货", description = "取消出货单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelShipment(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id,
            @Parameter(description = "取消原因") @RequestParam String reason) {

        shipmentService.cancelShipment(id, reason);
        return Result.ok("出货单已取消");
    }

    @Operation(summary = "更新物流信息", description = "更新出货单的物流跟踪信息")
    @PostMapping("/{id}/logistics")
    public Result<Void> updateLogistics(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id,
            @RequestBody Map<String, Object> logisticsInfo) {

        shipmentService.updateLogistics(id, logisticsInfo);
        return Result.ok("物流信息更新成功");
    }

    @Operation(summary = "获取物流跟踪", description = "获取出货单的物流跟踪信息")
    @GetMapping("/{id}/tracking")
    public Result<List<Map<String, Object>>> getShipmentTracking(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id) {

        List<Map<String, Object>> tracking = shipmentService.getShipmentTracking(id);
        return Result.ok(tracking);
    }

    @Operation(summary = "批量出货", description = "批量处理多个出货单")
    @PostMapping("/batch-ship")
    public Result<Map<String, Object>> batchShipment(@RequestBody List<Long> shipmentIds) {
        Map<String, Object> result = shipmentService.batchShipment(shipmentIds);
        return Result.ok(result, "批量出货处理完成");
    }

    @Operation(summary = "生成出货标签", description = "生成出货单的打印标签")
    @PostMapping("/{id}/generate-label")
    public Result<String> generateShippingLabel(
            @Parameter(description = "出货单ID", example = "1") @PathVariable Long id) {

        String labelUrl = shipmentService.generateShippingLabel(id);
        return Result.ok(labelUrl, "出货标签生成成功");
    }

    @Operation(summary = "获取出货统计", description = "获取出货相关统计数据")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getShipmentStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Map<String, Object> statistics = shipmentService.getShipmentStatistics(startDate, endDate);
        return Result.ok(statistics);
    }

    @Operation(summary = "导出出货单", description = "导出出货单数据")
    @PostMapping("/export")
    public Result<String> exportShipments(
            @RequestBody Map<String, Object> exportParams) {

        String exportPath = shipmentService.exportShipments(exportParams);
        return Result.ok(exportPath, "出货单导出成功");
    }
}