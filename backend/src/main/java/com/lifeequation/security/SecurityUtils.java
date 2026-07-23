package com.lifeequation.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing the current authenticated user from SecurityContextHolder.
 */
public class SecurityUtils {

    private SecurityUtils() {
        // utility class
    }

    /**
     * Get the current authenticated user's ID.
     * Returns null if not authenticated.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof UserDetailsImpl principal) {
            return principal.getUserId();
        }
        return null;
    }

    /**
     * Get the current authenticated user's username.
     * Returns null if not authenticated.
     */
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof UserDetailsImpl principal) {
            return principal.getUsername();
        }
        return auth.getName();
    }

    /**
     * Get the current authenticated user's role (without ROLE_ prefix).
     * Returns null if not authenticated.
     */
    public static String getCurrentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof UserDetailsImpl principal) {
            return principal.getRole();
        }
        return null;
    }

    /**
     * Check if the current user is an admin.
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(getCurrentRole());
    }

    /**
     * Check if the current user is a doctor.
     */
    public static boolean isDoctor() {
        return "DOCTOR".equals(getCurrentRole());
    }
}
