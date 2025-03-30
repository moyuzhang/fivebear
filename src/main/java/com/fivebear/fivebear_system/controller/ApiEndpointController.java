package com.fivebear.fivebear_system.controller;

import com.fivebear.fivebear_system.entity.ApiEndpoint;
import com.fivebear.fivebear_system.service.ApiEndpointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "API Endpoint Management", description = "API endpoints for managing API endpoints")
@RestController
@RequestMapping("/api/endpoints")
public class ApiEndpointController {

    @Autowired
    private ApiEndpointService apiEndpointService;

    @Operation(summary = "Get all API endpoints", description = "Retrieves a list of all API endpoints")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved endpoints",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class)))
    })
    @GetMapping
    public ResponseEntity<List<ApiEndpoint>> getAllEndpoints() {
        return ResponseEntity.ok(apiEndpointService.getAllEndpoints());
    }

    @Operation(summary = "Get endpoint by ID", description = "Retrieves a specific API endpoint by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved endpoint",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class))),
        @ApiResponse(responseCode = "404", description = "Endpoint not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiEndpoint> getEndpointById(
            @Parameter(description = "ID of the endpoint to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(apiEndpointService.getEndpointById(id));
    }

    @Operation(summary = "Create new endpoint", description = "Creates a new API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created endpoint",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class)))
    })
    @PostMapping
    public ResponseEntity<ApiEndpoint> createEndpoint(
            @Parameter(description = "API endpoint details") @RequestBody ApiEndpoint endpoint) {
        return ResponseEntity.ok(apiEndpointService.createEndpoint(endpoint));
    }

    @Operation(summary = "Update endpoint", description = "Updates an existing API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated endpoint",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class))),
        @ApiResponse(responseCode = "404", description = "Endpoint not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiEndpoint> updateEndpoint(
            @Parameter(description = "ID of the endpoint to update") @PathVariable Long id,
            @Parameter(description = "Updated endpoint details") @RequestBody ApiEndpoint endpoint) {
        return ResponseEntity.ok(apiEndpointService.updateEndpoint(id, endpoint));
    }

    @Operation(summary = "Delete endpoint", description = "Deletes an API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted endpoint"),
        @ApiResponse(responseCode = "404", description = "Endpoint not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @Parameter(description = "ID of the endpoint to delete") @PathVariable Long id) {
        apiEndpointService.deleteEndpoint(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Search endpoints", description = "Searches for API endpoints by keyword")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved endpoints",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ApiEndpoint>> searchEndpoints(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(apiEndpointService.searchEndpoints(keyword));
    }

    @Operation(summary = "Get endpoints by status", description = "Retrieves API endpoints filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved endpoints",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ApiEndpoint>> getEndpointsByStatus(
            @Parameter(description = "Status to filter by") @PathVariable String status) {
        return ResponseEntity.ok(apiEndpointService.getEndpointsByStatus(status));
    }

    @Operation(summary = "Get endpoints by method", description = "Retrieves API endpoints filtered by HTTP method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved endpoints",
                    content = @Content(schema = @Schema(implementation = ApiEndpoint.class)))
    })
    @GetMapping("/method/{method}")
    public ResponseEntity<List<ApiEndpoint>> getEndpointsByMethod(
            @Parameter(description = "HTTP method to filter by") @PathVariable String method) {
        return ResponseEntity.ok(apiEndpointService.getEndpointsByMethod(method));
    }

    @Operation(summary = "Get endpoint statistics", description = "Retrieves statistics about API endpoints")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    })
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEndpointStats() {
        return ResponseEntity.ok(apiEndpointService.getEndpointStats());
    }

    @Operation(summary = "Update endpoint status", description = "Updates the status of an API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated status"),
        @ApiResponse(responseCode = "404", description = "Endpoint not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateEndpointStatus(
            @Parameter(description = "ID of the endpoint") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status) {
        apiEndpointService.updateEndpointStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Test endpoint", description = "Tests an API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test completed successfully"),
        @ApiResponse(responseCode = "404", description = "Endpoint not found")
    })
    @PostMapping("/{id}/test")
    public ResponseEntity<Void> testEndpoint(
            @Parameter(description = "ID of the endpoint to test") @PathVariable Long id) {
        apiEndpointService.testEndpoint(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get test result", description = "Retrieves the test result for an API endpoint")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved test result")
    })
    @GetMapping("/{id}/test-result")
    public ResponseEntity<Map<String, Object>> getEndpointTestResult(
            @Parameter(description = "ID of the endpoint") @PathVariable Long id) {
        return ResponseEntity.ok(apiEndpointService.getEndpointTestResult(id));
    }
} 