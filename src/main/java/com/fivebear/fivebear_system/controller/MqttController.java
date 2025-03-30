package com.fivebear.fivebear_system.controller;

import com.fivebear.fivebear_system.common.ApiResponse;
import com.fivebear.fivebear_system.model.MqttMessage;
import com.fivebear.fivebear_system.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mqtt")
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @PostMapping("/publish")
    public ResponseEntity<ApiResponse<?>> publishMessage(
            @RequestParam String topic,
            @RequestBody String message) {
        try {
            mqttService.publish(topic, message);
            return ResponseEntity.ok(ApiResponse.success("消息发送成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("消息发送失败: " + e.getMessage()));
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<?>> subscribeTopic(
            @RequestParam String topic,
            @RequestParam(defaultValue = "1") int qos) {
        try {
            mqttService.subscribe(topic, qos);
            return ResponseEntity.ok(ApiResponse.success("订阅成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("订阅失败: " + e.getMessage()));
        }
    }

    @PostMapping("/subscribe/all")
    public ResponseEntity<ApiResponse<?>> subscribeAllTopics() {
        try {
            mqttService.subscribeAll();
            return ResponseEntity.ok(ApiResponse.success("订阅所有主题成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("订阅失败: " + e.getMessage()));
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<MqttMessage>>> getMessageHistory() {
        try {
            List<MqttMessage> messages = mqttService.getMessageHistory();
            return ResponseEntity.ok(ApiResponse.success(messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取消息历史失败: " + e.getMessage()));
        }
    }
} 