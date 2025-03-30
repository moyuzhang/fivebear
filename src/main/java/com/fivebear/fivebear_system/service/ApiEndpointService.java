package com.fivebear.fivebear_system.service;

import com.fivebear.fivebear_system.entity.ApiEndpoint;
import java.util.List;
import java.util.Map;

public interface ApiEndpointService {
    List<ApiEndpoint> getAllEndpoints();
    ApiEndpoint getEndpointById(Long id);
    ApiEndpoint createEndpoint(ApiEndpoint endpoint);
    ApiEndpoint updateEndpoint(Long id, ApiEndpoint endpoint);
    void deleteEndpoint(Long id);
    List<ApiEndpoint> searchEndpoints(String keyword);
    List<ApiEndpoint> getEndpointsByStatus(String status);
    List<ApiEndpoint> getEndpointsByMethod(String method);
    Map<String, Object> getEndpointStats();
    void updateEndpointStatus(Long id, String status);
    void testEndpoint(Long id);
    Map<String, Object> getEndpointTestResult(Long id);
} 