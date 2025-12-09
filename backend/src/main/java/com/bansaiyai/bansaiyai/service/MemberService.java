package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing member operations.
 * Handles member CRUD operations and business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;

  public Page<Member> getAllMembers(Pageable pageable) {
    log.debug("Fetching all members with pagination: {}", pageable);
    return memberRepository.findAll(pageable);
  }

  public Optional<Member> getMemberById(Long id) {
    log.debug("Fetching member by id: {}", id);
    return memberRepository.findById(id);
  }

  @Transactional
  public Member saveMember(Member member) {
    log.info("Saving member: {}", member.getMemberId());
    return memberRepository.save(member);
  }

  @Transactional
  public void deleteMember(Long id) {
    log.info("Deleting member with id: {}", id);
    memberRepository.deleteById(id);
  }

  public List<Member> searchMembers(String keyword) {
    return memberRepository.searchMembers(keyword);
  }

  public List<Member> getActiveMembers() {
    return memberRepository.findByIsActive(true);
  }

  public List<Member> getInactiveMembers() {
    return memberRepository.findByIsActive(false);
  }

  public Optional<Member> getMemberByUserUserId(Long userId) {
    return memberRepository.findByUserId(userId);
  }

  public MemberStatistics getMemberStatistics() {
    long totalMembers = memberRepository.count();
    long activeMembers = memberRepository.countActiveMembers();
    long inactiveMembers = memberRepository.countInactiveMembers();

    return MemberStatistics.builder()
        .totalMembers(totalMembers)
        .activeMembers(activeMembers)
        .inactiveMembers(inactiveMembers)
        .build();
  }

  @lombok.Data
  @lombok.Builder
  public static class MemberStatistics {
    private long totalMembers;
    private long activeMembers;
    private long inactiveMembers;
  }
}
