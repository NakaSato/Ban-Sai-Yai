package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.User;
import org.springframework.stereotype.Service;

@Service
public class MemberAccessEvaluator {

    /**
     * Check if a user can view a specific member's data.
     * 
     * @param user     The user attempting to access the data
     * @param memberId The ID of the member data being accessed
     * @return true if access is allowed, false otherwise
     */
    public boolean canViewMember(User user, Long memberId) {
        if (user == null) {
            return false;
        }

        // Admins and Officers can view all members
        if (user.getRole() == User.Role.PRESIDENT ||
                user.getRole() == User.Role.SECRETARY ||
                user.getRole() == User.Role.OFFICER ||
                user.getRole() == User.Role.ADMIN) {
            return true;
        }

        // Regular members can only view their own data
        if (user.getRole() == User.Role.MEMBER) {
            return user.getMember() != null && user.getMember().getId().equals(memberId);
        }

        return false;
    }
}
