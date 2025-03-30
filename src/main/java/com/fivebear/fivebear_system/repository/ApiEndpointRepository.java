package com.fivebear.fivebear_system.repository;

import com.fivebear.fivebear_system.entity.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {
    List<ApiEndpoint> findByStatus(String status);
    List<ApiEndpoint> findByMethod(String method);
    List<ApiEndpoint> findByPathContaining(String path);
    List<ApiEndpoint> findByNameContaining(String name);
} 