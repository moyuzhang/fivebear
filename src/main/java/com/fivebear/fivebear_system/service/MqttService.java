package com.fivebear.fivebear_system.service;

import com.fivebear.fivebear_system.model.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class MqttService {

    @Autowired
    private MqttClient mqttClient;

    private final List<String> defaultTopics = Arrays.asList("test/topic", "system/status", "device/data");
    private final List<MqttMessage> messageHistory = new CopyOnWriteArrayList<>();

    public void publish(String topic, String message) {
        try {
            org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage = new org.eclipse.paho.client.mqttv3.MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            mqttClient.publish(topic, mqttMessage);
            log.info("消息发送成功 - Topic: {}, Message: {}", topic, message);
            
            // 保存消息历史
            MqttMessage msg = new MqttMessage();
            msg.setTopic(topic);
            msg.setMessage(message);
            msg.setQos(1);
            msg.setTimestamp(System.currentTimeMillis());
            messageHistory.add(msg);
            
            // 保持历史记录在合理范围内
            if (messageHistory.size() > 100) {
                messageHistory.remove(0);
            }
        } catch (MqttException e) {
            log.error("消息发送失败 - Topic: {}, Message: {}, Error: {}", topic, message, e.getMessage());
        }
    }

    public void subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos, (t, msg) -> {
                String message = new String(msg.getPayload());
                log.info("收到消息 - Topic: {}, Message: {}", t, message);
                handleMessage(t, message);
            });
            log.info("订阅成功 - Topic: {}", topic);
        } catch (MqttException e) {
            log.error("订阅失败 - Topic: {}, Error: {}", topic, e.getMessage());
        }
    }

    public void subscribeAll() {
        defaultTopics.forEach(topic -> subscribe(topic, 1));
    }

    public List<MqttMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }

    private void handleMessage(String topic, String message) {
        // 在这里处理接收到的消息
        log.info("处理消息 - Topic: {}, Message: {}", topic, message);
        
        // 保存消息历史
        MqttMessage msg = new MqttMessage();
        msg.setTopic(topic);
        msg.setMessage(message);
        msg.setQos(1);
        msg.setTimestamp(System.currentTimeMillis());
        messageHistory.add(msg);
        
        // 保持历史记录在合理范围内
        if (messageHistory.size() > 100) {
            messageHistory.remove(0);
        }
    }
} 