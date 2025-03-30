package com.fivebear.fivebear_system.service.impl;

import com.fivebear.fivebear_system.entity.ApiEndpoint;
import com.fivebear.fivebear_system.repository.ApiEndpointRepository;
import com.fivebear.fivebear_system.service.ApiEndpointService;
import com.fivebear.fivebear_system.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiEndpointServiceImpl implements ApiEndpointService {

    @Autowired
    private ApiEndpointRepository apiEndpointRepository;

    private final Map<Long, Map<String, Object>> testResults = new ConcurrentHashMap<>();

    @Override
    public List<ApiEndpoint> getAllEndpoints() {
        return apiEndpointRepository.findAll();
    }

    @Override
    public ApiEndpoint getEndpointById(Long id) {
        return apiEndpointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("API endpoint not found"));
    }

    @Override
    @Transactional
    public ApiEndpoint createEndpoint(ApiEndpoint endpoint) {
        endpoint.setStatus("ACTIVE");
        return apiEndpointRepository.save(endpoint);
    }

    @Override
    @Transactional
    public ApiEndpoint updateEndpoint(Long id, ApiEndpoint endpoint) {
        ApiEndpoint existingEndpoint = getEndpointById(id);
        existingEndpoint.setName(endpoint.getName());
        existingEndpoint.setPath(endpoint.getPath());
        existingEndpoint.setMethod(endpoint.getMethod());
        existingEndpoint.setDescription(endpoint.getDescription());
        existingEndpoint.setRequestParams(endpoint.getRequestParams());
        existingEndpoint.setResponseFormat(endpoint.getResponseFormat());
        existingEndpoint.setExampleRequest(endpoint.getExampleRequest());
        existingEndpoint.setExampleResponse(endpoint.getExampleResponse());
        return apiEndpointRepository.save(existingEndpoint);
    }

    @Override
    @Transactional
    public void deleteEndpoint(Long id) {
        apiEndpointRepository.deleteById(id);
    }

    @Override
    public List<ApiEndpoint> searchEndpoints(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEndpoints();
        }
        List<ApiEndpoint> results = new ArrayList<>();
        results.addAll(apiEndpointRepository.findByNameContaining(keyword));
        results.addAll(apiEndpointRepository.findByPathContaining(keyword));
        return results;
    }

    @Override
    public List<ApiEndpoint> getEndpointsByStatus(String status) {
        return apiEndpointRepository.findByStatus(status);
    }

    @Override
    public List<ApiEndpoint> getEndpointsByMethod(String method) {
        return apiEndpointRepository.findByMethod(method);
    }

    @Override
    public Map<String, Object> getEndpointStats() {
        Map<String, Object> stats = new HashMap<>();
        List<ApiEndpoint> allEndpoints = getAllEndpoints();
        
        stats.put("totalEndpoints", allEndpoints.size());
        stats.put("activeEndpoints", getEndpointsByStatus("ACTIVE").size());
        stats.put("inactiveEndpoints", getEndpointsByStatus("INACTIVE").size());
        
        Map<String, Long> methodStats = new HashMap<>();
        allEndpoints.forEach(endpoint -> 
            methodStats.merge(endpoint.getMethod(), 1L, Long::sum));
        stats.put("methodStats", methodStats);
        
        return stats;
    }

    @Override
    @Transactional
    public void updateEndpointStatus(Long id, String status) {
        ApiEndpoint endpoint = getEndpointById(id);
        endpoint.setStatus(status);
        apiEndpointRepository.save(endpoint);
    }

    @Override
    public void testEndpoint(Long id) {
        ApiEndpoint endpoint = getEndpointById(id);
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            String response = HttpClientUtils.get(endpoint.getPath());
            long endTime = System.currentTimeMillis();
            
            result.put("success", true);
            result.put("responseTime", endTime - startTime);
            result.put("response", response);
            result.put("timestamp", new Date());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", new Date());
        }
        
        testResults.put(id, result);
    }

    @Override
    public Map<String, Object> getEndpointTestResult(Long id) {
        return testResults.getOrDefault(id, new HashMap<>());
    }
} 