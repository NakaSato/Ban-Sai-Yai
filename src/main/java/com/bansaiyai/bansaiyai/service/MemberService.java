package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.Member;
import com.bansaiyai.bansaiyai.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

  @Autowired
  private MemberRepository memberRepository;

  public Page<Member> getAllMembers(Pageable pageable) {
    return memberRepository.findAll(pageable);
  }

  public Optional<Member> getMemberById(Long id) {
    return memberRepository.findById(id);
  }

  public Member saveMember(Member member) {
    return memberRepository.save(member);
  }

  public void deleteMember(Long id) {
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
}
