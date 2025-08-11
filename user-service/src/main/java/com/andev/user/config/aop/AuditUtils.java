package com.andev.user.config.aop;

public class AuditUtils {

    public static String getUserName() {
        //    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //    if (auth != null && !auth.getPrincipal().equals("anonymousUser")) {
        //      User user = (User) auth.getPrincipal();
        //      return user.getUsername();
        //    }
        return "system";
    }
}
