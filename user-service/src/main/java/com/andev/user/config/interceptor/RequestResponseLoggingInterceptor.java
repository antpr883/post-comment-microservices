package com.andev.user.config.interceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP Request/Response logging interceptor for User Service.
 *
 * Logs comprehensive request/response information including:
 * - HTTP method, URL, headers
 * - Request parameters (with sensitive data masking)
 * - Response status and execution time
 * - Correlation ID for request tracking
 * - Performance metrics
 */
@Slf4j
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final Logger REQUEST_LOGGER = LoggerFactory.getLogger("REQUEST");
    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");

    @Value("${app.logging.request.max-body-length:1000}")
    private int maxBodyLength;

    @Value("${app.logging.request.include-headers:true}")
    private boolean includeHeaders;

    @Value("${app.performance.slow-request-threshold:2000}")
    private long slowRequestThreshold;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Generate correlation ID for request tracking
        String correlationId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("correlationId", correlationId);

        // Store in request attributes for postHandle
        request.setAttribute("startTime", System.currentTimeMillis());
        request.setAttribute("stopWatch", stopWatch);
        request.setAttribute("correlationId", correlationId);

        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            requestData.put("correlationId", correlationId);
            requestData.put("method", request.getMethod());
            requestData.put("url", request.getRequestURL().toString());
            requestData.put("queryString", request.getQueryString());
            requestData.put("remoteAddr", getClientIpAddress(request));
            requestData.put("userAgent", request.getHeader("User-Agent"));

            // Add headers if enabled
            if (includeHeaders) {
                requestData.put("headers", sanitizeHeaders(request));
            }

            // Add request parameters
            if (!request.getParameterMap().isEmpty()) {
                requestData.put("parameters", sanitizeParameters(request.getParameterMap()));
            }

            REQUEST_LOGGER.info("REQUEST_START: {}", objectMapper.writeValueAsString(requestData));

        } catch (Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        try {
            StopWatch stopWatch = (StopWatch) request.getAttribute("stopWatch");
            String correlationId = (String) request.getAttribute("correlationId");

            if (stopWatch != null) {
                stopWatch.stop();
            }

            long executionTime = stopWatch != null
                    ? stopWatch.getTotalTimeMillis()
                    : System.currentTimeMillis() - (Long) request.getAttribute("startTime");

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            responseData.put("correlationId", correlationId);
            responseData.put("method", request.getMethod());
            responseData.put("url", request.getRequestURL().toString());
            responseData.put("status", response.getStatus());
            responseData.put("executionTimeMs", executionTime);

            if (ex != null) {
                responseData.put("exception", ex.getClass().getSimpleName());
                responseData.put("exceptionMessage", ex.getMessage());
                REQUEST_LOGGER.error("REQUEST_ERROR: {}", objectMapper.writeValueAsString(responseData));
            } else {
                REQUEST_LOGGER.info("REQUEST_COMPLETE: {}", objectMapper.writeValueAsString(responseData));
            }

            // Performance logging for slow requests
            if (executionTime > slowRequestThreshold) {
                PERFORMANCE_LOGGER.warn(
                        "SLOW_REQUEST: {} {} took {}ms (threshold: {}ms)",
                        request.getMethod(),
                        request.getRequestURL(),
                        executionTime,
                        slowRequestThreshold);
            }

        } catch (Exception e) {
            log.warn("Failed to log response: {}", e.getMessage());
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /**
     * Extract real client IP address considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Sanitize headers by masking sensitive information
     */
    private Map<String, String> sanitizeHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Mask sensitive headers
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "***MASKED***");
            } else {
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }

    /**
     * Sanitize request parameters by masking sensitive data
     */
    private Map<String, Object> sanitizeParameters(Map<String, String[]> parameterMap) {
        Map<String, Object> parameters = new HashMap<>();

        parameterMap.forEach((key, values) -> {
            if (isSensitiveParameter(key)) {
                parameters.put(key, "***MASKED***");
            } else {
                parameters.put(key, values.length == 1 ? values[0] : values);
            }
        });

        return parameters;
    }

    /**
     * Check if header contains sensitive information
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return lowerCaseName.contains("authorization")
                || lowerCaseName.contains("token")
                || lowerCaseName.contains("password")
                || lowerCaseName.contains("secret");
    }

    /**
     * Check if parameter contains sensitive information
     */
    private boolean isSensitiveParameter(String paramName) {
        String lowerCaseName = paramName.toLowerCase();
        return lowerCaseName.contains("password")
                || lowerCaseName.contains("token")
                || lowerCaseName.contains("secret")
                || lowerCaseName.contains("key");
    }
}
