package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.service.MemberService;
import com.bansaiyai.bansaiyai.dto.PaymentResponse;
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
import java.util.UUID;

@RestController
@RequestMapping("/members")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MemberController {

  @Autowired
  private MemberService memberService;

  @Autowired
  private com.bansaiyai.bansaiyai.service.UserService userService;

  @Autowired
  private com.bansaiyai.bansaiyai.service.MemberAccessEvaluator memberAccessEvaluator;

  @Autowired
  private com.bansaiyai.bansaiyai.service.PaymentService paymentService;

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

  /**
   * Get member by UUID (SECURE - prevents ID enumeration)
   * 
   * @param uuid           Member's UUID
   * @param authentication Current user authentication
   * @return Member details if authorized
   */
  @GetMapping("/{uuid}")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<Member> getMemberById(@PathVariable UUID uuid, Authentication authentication) {
    // Get current user
    User currentUser = getCurrentUser(authentication);

    // Find member by UUID
    return memberService.getMemberByUuid(uuid)
        .map(member -> {
          // Check access using evaluator
          if (!memberAccessEvaluator.canViewMember(currentUser, member.getId())) {
            return ResponseEntity.status(403).<Member>build();
          }
          return ResponseEntity.ok(member);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Get member transactions by UUID (SECURE)
   */
  @GetMapping("/{uuid}/transactions")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<Page<PaymentResponse>> getMemberTransactions(
      @PathVariable UUID uuid,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "paymentDate") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir,
      Authentication authentication) {

    User currentUser = getCurrentUser(authentication);

    return memberService.getMemberByUuid(uuid)
        .map(member -> {
          // Check access using evaluator
          if (!memberAccessEvaluator.canViewMember(currentUser, member.getId())) {
            return ResponseEntity.status(403).<Page<PaymentResponse>>build();
          }

          Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
          Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

          return ResponseEntity.ok(paymentService.getPaymentsByMember(member.getId(), pageable));
        })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/my-profile")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER')")
  public ResponseEntity<Member> getCurrentMember(Authentication authentication) {
    User currentUser = getCurrentUser(authentication);
    if (currentUser.getMember() != null) {
      return ResponseEntity.ok(currentUser.getMember());
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
  public ResponseEntity<Member> createMember(@RequestBody Member member) {
    Member savedMember = memberService.saveMember(member);
    return ResponseEntity.ok(savedMember);
  }

  /**
   * Update member by UUID (SECURE)
   * 
   * @param uuid   Member's UUID
   * @param member Updated member data
   * @return Updated member
   */
  @PutMapping("/{uuid}")
  @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY')")
  public ResponseEntity<Member> updateMember(@PathVariable UUID uuid, @RequestBody Member member) {
    return memberService.getMemberByUuid(uuid)
        .map(existingMember -> {
          member.setId(existingMember.getId());
          member.setUuid(uuid); // Preserve UUID
          Member updatedMember = memberService.saveMember(member);
          return ResponseEntity.ok(updatedMember);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Delete member by UUID (SECURE)
   * 
   * @param uuid Member's UUID
   * @return Success response
   */
  @DeleteMapping("/{uuid}")
  @PreAuthorize("hasRole('PRESIDENT')")
  public ResponseEntity<Void> deleteMember(@PathVariable UUID uuid) {
    memberService.deleteMemberByUuid(uuid);
    return ResponseEntity.noContent().build();
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
   */
  private User getCurrentUser(Authentication authentication) {
    String username = authentication.getName();
    return userService.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
  }
}
