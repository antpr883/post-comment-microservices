package com.andev.post.web.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health Check", description = "Health monitoring endpoints")
@RequestMapping("/health")
public interface HealthEndpoints {

    @Operation(summary = "Health check", description = "Returns the health status of the post service")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service is healthy")})
    @GetMapping
    ResponseEntity<String> healthCheck();
}
