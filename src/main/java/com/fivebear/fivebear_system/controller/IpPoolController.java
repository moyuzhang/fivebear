package com.fivebear.fivebear_system.controller;

import com.fivebear.fivebear_system.common.ApiResponse;
import com.fivebear.fivebear_system.entity.IpPoolSettings;
import com.fivebear.fivebear_system.model.IpInfo;
import com.fivebear.fivebear_system.service.IpPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IpPoolController {

    @Autowired
    private IpPoolService ipPoolService;

    @GetMapping("/ip-pool/status")
    public ResponseEntity<?> getIpPoolStatus() {
        Map<String, Object> status = ipPoolService.getStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/ip-pool/start")
    public ResponseEntity<?> startIpPool() {
        ipPoolService.start();
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已启动"));
    }

    @PostMapping("/ip-pool/stop")
    public ResponseEntity<?> stopIpPool() {
        ipPoolService.stop();
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已停止"));
    }

    @PostMapping("/ip-pool/refresh")
    public ResponseEntity<?> refreshIpPool() {
        ipPoolService.refresh();
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已刷新"));
    }

    @GetMapping("/IpSettings")
    public ResponseEntity<ApiResponse<?>> getIpPoolSettings() {
        IpPoolSettings settings = ipPoolService.getSettings();
        return ResponseEntity.ok(ApiResponse.success("获取设置成功", settings));
    }

    @PutMapping("/IpSettings")
    public ResponseEntity<ApiResponse<?>> saveIpPoolSettings(@RequestBody IpPoolSettings settings) {
        ipPoolService.saveSettings(settings);
        return ResponseEntity.ok(ApiResponse.success("设置已保存"));
    }

    @GetMapping("/proxy/list")
    public ResponseEntity<?> getIpList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Boolean validStatus,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = ipPoolService.getIpList(page, pageSize, validStatus, keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/proxy/fastest")
    public ResponseEntity<?> getFastestIp() {
        IpInfo ip = ipPoolService.getFastestIp();
        return ResponseEntity.ok(ip);
    }

    @PostMapping("/proxy")
    public ResponseEntity<?> addProxy(@RequestBody Map<String, Object> proxy) {
        ipPoolService.addProxy(proxy);
        return ResponseEntity.ok(Map.of("success", true, "message", "代理IP已添加"));
    }

    @DeleteMapping("/proxy")
    public ResponseEntity<?> deleteProxy(@RequestBody Map<String, Object> proxy) {
        ipPoolService.deleteProxy(proxy);
        return ResponseEntity.ok(Map.of("success", true, "message", "代理IP已删除"));
    }

    @DeleteMapping("/proxy/batch")
    public ResponseEntity<?> batchDeleteProxies(@RequestBody Map<String, List<Map<String, Object>>> request) {
        ipPoolService.batchDeleteProxies(request.get("proxies"));
        return ResponseEntity.ok(Map.of("success", true, "message", "代理IP已批量删除"));
    }

    @PostMapping("/proxy/test")
    public ResponseEntity<?> testProxy(@RequestBody Map<String, Object> proxy) {
        Map<String, Object> result = ipPoolService.testProxy(proxy);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/proxy/quick-test")
    public ResponseEntity<?> quickTestProxy(@RequestBody Map<String, Object> testInfo) {
        Map<String, Object> result = ipPoolService.quickTestProxy(testInfo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ip-pool/stats")
    public ResponseEntity<?> getIpPoolStats() {
        Map<String, Object> stats = ipPoolService.getStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/ip-pool/info")
    public ResponseEntity<?> getIpPoolInfo() {
        Map<String, Object> info = ipPoolService.getInfo();
        return ResponseEntity.ok(info);
    }

    @GetMapping("/ip-pool/list")
    public ResponseEntity<?> getIpPoolList(@RequestParam Map<String, Object> params) {
        Map<String, Object> result = ipPoolService.getIpPoolList(params);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ip-pool/add")
    public ResponseEntity<?> addIpPool(@RequestBody Map<String, Object> data) {
        ipPoolService.addIpPool(data);
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已添加"));
    }

    @PutMapping("/ip-pool/update")
    public ResponseEntity<?> updateIpPool(@RequestBody Map<String, Object> data) {
        ipPoolService.updateIpPool(data);
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已更新"));
    }

    @DeleteMapping("/ip-pool/delete/{id}")
    public ResponseEntity<?> deleteIpPool(@PathVariable String id) {
        ipPoolService.deleteIpPool(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "IP池已删除"));
    }
} 