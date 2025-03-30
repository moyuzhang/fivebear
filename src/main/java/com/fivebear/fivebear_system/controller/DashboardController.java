package com.fivebear.fivebear_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/data")
    public ResponseEntity<List<Map<String, String>>> getDashboardData() {
        List<Map<String, String>> data = new ArrayList<>();
        
        // 添加示例数据
        Map<String, String> item1 = new HashMap<>();
        item1.put("date", "2024-03-23");
        item1.put("name", "总IP数");
        item1.put("value", "1000");
        data.add(item1);

        Map<String, String> item2 = new HashMap<>();
        item2.put("date", "2024-03-23");
        item2.put("name", "有效IP数");
        item2.put("value", "800");
        data.add(item2);

        Map<String, String> item3 = new HashMap<>();
        item3.put("date", "2024-03-23");
        item3.put("name", "平均响应时间");
        item3.put("value", "200ms");
        data.add(item3);

        return ResponseEntity.ok(data);
    }
} 