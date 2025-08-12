package com.andev.user.config.aop;

/**
 * Simple utility class for audit logging operations.
 * Provides basic context extraction for audit logs.
 */
public class AuditUtils {

    /**
     * Get current user name from context.
     * Returns "system" if no user context is available.
     */
    public static String getUserName() {
        // In a real implementation, this would extract from SecurityContext
        return "system";
    }

    /**
     * Get client IP address from request.
     * Returns "unknown" if not available.
     */
    public static String getIpAddress() {
        // In a real implementation, this would extract from HttpServletRequest
        return "unknown";
    }

    /**
     * Get user agent from request.
     * Returns "unknown" if not available.
     */
    public static String getUserAgent() {
        // In a real implementation, this would extract from HttpServletRequest
        return "unknown";
    }
}
