package com.fivebear.fivebear_system.model;

import lombok.Data;

@Data
public class MqttMessage {
    private String topic;
    private String message;
    private Integer qos;
    private Long timestamp;
} 