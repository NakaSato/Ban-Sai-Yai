package com.bansaiyai.bansaiyai.property;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.MemberAccessEvaluator;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 13: Member data isolation
 * Validates: Requirements 6.1, 6.2
 *
 * This test verifies that:
 * 1. Members can always view their own data.
 * 2. Members can never view other members' data (unless they are
 * admins/officers).
 * 3. Admins/Officers can view any member data.
 */
public class Property13_MemberDataIsolationTest {

    private final MemberAccessEvaluator evaluator = new MemberAccessEvaluator();

    @Property
    void membersCanAlwaysViewOwnProfile(@ForAll("members") User user) {
        // Arrange
        // (User is a MEMBER role with a linked Member entity)

        // Act
        boolean canView = evaluator.canViewMember(user, user.getMember().getId());

        // Assert
        assertThat(canView).isTrue();
    }

    @Property
    void membersCannotViewOtherProfiles(
            @ForAll("members") User user,
            @ForAll("members") User otherUser) {

        // Arrange
        // Ensure IDs are different
        Assume.that(!user.getMember().getId().equals(otherUser.getMember().getId()));

        // Act
        boolean canView = evaluator.canViewMember(user, otherUser.getMember().getId());

        // Assert
        assertThat(canView).isFalse();
    }

    @Property
    void privilegedRolesCanViewAnyProfile(
            @ForAll("privilegedUsers") User privilegedUser,
            @ForAll Long randomMemberId) {

        // Act
        boolean canView = evaluator.canViewMember(privilegedUser, randomMemberId);

        // Assert
        assertThat(canView).isTrue();
    }

    @Provide
    Arbitrary<User> members() {
        return Arbitraries.longs().between(1, 10000).flatMap(id -> {
            Member member = new Member();
            member.setId(id);

            return Arbitraries.strings().alpha().ofMinLength(5).map(username -> {
                User user = new User();
                user.setUsername(username);
                user.setRole(User.Role.MEMBER);
                user.setMember(member);
                return user;
            });
        });
    }

    @Provide
    Arbitrary<User> privilegedUsers() {
        Arbitrary<User.Role> roles = Arbitraries.of(
                User.Role.PRESIDENT,
                User.Role.SECRETARY,
                User.Role.OFFICER,
                User.Role.OFFICER);

        return roles.flatMap(role -> Arbitraries.strings().alpha().ofMinLength(5).map(username -> {
            User user = new User();
            user.setUsername(username);
            // privileged users might not have a linked member object, or it shouldn't
            // matter
            user.setRole(role);
            return user;
        }));
    }
}
