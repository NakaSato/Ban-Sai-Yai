package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MemberController {

  @Autowired
  private MemberService memberService;

  @GetMapping
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<Page<Member>> getAllMembers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    Page<Member> members = memberService.getAllMembers(pageable);
    return ResponseEntity.ok(members);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<Member> getMemberById(@PathVariable Long id, Authentication authentication) {
    // Get current user
    User currentUser = getCurrentUser(authentication);

    // Member data isolation: Members can only view their own data
    if (currentUser.getRole() == User.Role.MEMBER) {
      // Check if the member is viewing their own profile
      if (currentUser.getMember() == null || !currentUser.getMember().getId().equals(id)) {
        return ResponseEntity.status(403).build();
      }
    }

    return memberService.getMemberById(id)
        .map(member -> ResponseEntity.ok(member))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/my-profile")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<Member> getCurrentMember() {
    // This would typically get the current user's ID from security context
    // For now, we'll implement a basic version
    return ResponseEntity.ok().build();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Member> createMember(@RequestBody Member member) {
    Member savedMember = memberService.saveMember(member);
    return ResponseEntity.ok(savedMember);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
    return memberService.getMemberById(id)
        .map(existingMember -> {
          member.setId(id);
          Member updatedMember = memberService.saveMember(member);
          return ResponseEntity.ok(updatedMember);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
    memberService.deleteMember(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/search")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<List<Member>> searchMembers(@RequestParam String keyword) {
    List<Member> members = memberService.searchMembers(keyword);
    return ResponseEntity.ok(members);
  }

  @GetMapping("/active")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<List<Member>> getActiveMembers() {
    List<Member> activeMembers = memberService.getActiveMembers();
    return ResponseEntity.ok(activeMembers);
  }

  @GetMapping("/inactive")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<List<Member>> getInactiveMembers() {
    List<Member> inactiveMembers = memberService.getInactiveMembers();
    return ResponseEntity.ok(inactiveMembers);
  }

  @GetMapping("/statistics")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<MemberService.MemberStatistics> getMemberStatistics() {
    MemberService.MemberStatistics stats = memberService.getMemberStatistics();
    return ResponseEntity.ok(stats);
  }

  /**
   * Helper method to get current user from authentication
   * In a real implementation, you'd fetch this from database
   */
  private User getCurrentUser(Authentication authentication) {
    // This is a simplified approach. In a real application, you'd:
    // 1. Get the current user from database
    // 2. Return the actual User object

    // For demo purposes, we'll create a mock user
    User mockUser = new User();
    mockUser.setUsername(authentication.getName());

    // Set role based on the authenticated user
    // In reality, this would come from the database
    mockUser.setRole(User.Role.MEMBER); // Default to member for demo

    return mockUser;
  }
}
