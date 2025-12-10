package com.bansaiyai.bansaiyai.security;

import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user's username.
     * Returns "system" if no authentication is found.
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return "system";
    }

    /**
     * Get the currently authenticated User entity.
     * Throws an exception if user is not authenticated or not found.
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if ("system".equals(username)) {
            // In a real scenario, you might want to return a specific system user entity or
            // throw
            // For now, let's try to find a "system" user or throw
            return userRepository.findByUsername("system")
                    .orElseThrow(() -> new IllegalStateException(
                            "Current user is not authenticated or system user not found"));
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    /**
     * Check if the currently authenticated user matches the given member ID (user
     * ID).
     */
    public boolean isCurrentMember(Long memberId) {
        try {
            Long currentUserId = getCurrentUser().getId();
            return currentUserId != null && currentUserId.equals(memberId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the current user has the specified role.
     * 
     * @param roleName The role name to check (e.g., "PRESIDENT", "SECRETARY")
     * @return true if the user has the specified role
     */
    public boolean hasRole(String roleName) {
        try {
            User user = getCurrentUser();
            if (user == null || user.getRole() == null) {
                return false;
            }
            return user.getRole().name().equals(roleName) ||
                    user.getRole().name().equals("ROLE_" + roleName) ||
                    ("ROLE_" + user.getRole().name()).equals(roleName);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the member ID of the currently authenticated user.
     * 
     * @return the member ID, or null if not available
     */
    public Long getCurrentMemberId() {
        try {
            User user = getCurrentUser();
            if (user != null && user.getMember() != null) {
                return user.getMember().getId();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the user ID of the currently authenticated user.
     * 
     * @return the user ID, or null if not available
     */
    public Long getCurrentUserId() {
        try {
            User user = getCurrentUser();
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
