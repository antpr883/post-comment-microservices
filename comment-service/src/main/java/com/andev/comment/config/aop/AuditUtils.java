package com.andev.comment.config.aop;

import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for audit logging operations.
 * Provides context extraction and data sanitization for audit logs.
 *
 * @author Comment Service Team
 * @since 1.0
 */
public class AuditUtils {

    private static final String SYSTEM_USER = "system";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String USER_ID_KEY = "userId";
    private static final String USER_NAME_KEY = "username";

    private static final ThreadLocal<String> THREAD_LOCAL_CONTEXT = new ThreadLocal<>();

    // Patterns for sensitive data detection
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("password|pwd|secret|token", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");

    /**
     * Get or generate correlation ID for request tracking.
     */
    public static String getCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
        return correlationId;
    }

    /**
     * Get or generate request ID.
     */
    public static String getRequestId() {
        String requestId = MDC.get(REQUEST_ID_KEY);
        if (requestId == null) {
            requestId = generateRequestId();
            MDC.put(REQUEST_ID_KEY, requestId);
        }
        return requestId;
    }

    /**
     * Get current user ID from security context.
     */
    public static String getUserId() {
        String userId = MDC.get(USER_ID_KEY);
        if (userId == null) {
            // TODO: Extract from Security Context when implemented
            return "anonymous";
        }
        return userId;
    }

    /**
     * Get current username from security context.
     */
    public static String getUserName() {
        String username = MDC.get(USER_NAME_KEY);
        if (username == null) {
            // TODO: Extract from Security Context when implemented
            return SYSTEM_USER;
        }
        return username;
    }

    /**
     * Get client IP address from request.
     */
    public static String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Fall back to unknown if request context not available
        }
        return "unknown";
    }

    /**
     * Get user agent from request.
     */
    public static String getUserAgent() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Fall back to unknown if request context not available
        }
        return "unknown";
    }

    /**
     * Check if this is an internal service call.
     */
    public static boolean isInternalServiceCall() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String serviceName = request.getHeader("X-Service-Name");
                String serviceSecret = request.getHeader("X-Service-Secret");
                return serviceName != null && serviceSecret != null;
            }
        } catch (Exception e) {
            // Fall back to false
        }
        return false;
    }

    /**
     * Sanitize sensitive data from objects.
     */
    public static String sanitize(Object obj) {
        if (obj == null) {
            return "null";
        }

        String data = obj.toString();

        // Mask passwords and secrets
        if (PASSWORD_PATTERN.matcher(data).find()) {
            return "[SENSITIVE_DATA_MASKED]";
        }

        // Partially mask emails
        data = EMAIL_PATTERN.matcher(data).replaceAll(email -> maskEmail(email.group()));

        // Partially mask phone numbers
        data = PHONE_PATTERN.matcher(data).replaceAll(phone -> maskPhone(phone.group()));

        return data;
    }

    /**
     * Setup audit context for current thread.
     */
    public static void setupAuditContext() {
        getCorrelationId();
        getRequestId();
    }

    /**
     * Clear audit context.
     */
    public static void clearAuditContext() {
        MDC.clear();
        THREAD_LOCAL_CONTEXT.remove();
    }

    /**
     * Add context data to MDC.
     */
    public static void addContext(String key, String value) {
        MDC.put(key, value);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private static String generateCorrelationId() {
        return "CORR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String maskEmail(String email) {
        if (email.length() <= 3) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) return "***";
        return email.substring(0, 1) + "***" + email.substring(atIndex);
    }

    private static String maskPhone(String phone) {
        if (phone.length() <= 4) return "****";
        return "***-***-" + phone.substring(phone.length() - 4);
    }
}
