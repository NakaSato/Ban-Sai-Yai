package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.RiskProfileDTO;
import com.bansaiyai.bansaiyai.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member-risk")
@RequiredArgsConstructor
public class MemberRiskController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'SECRETARY', 'OFFICER')")
    public ResponseEntity<RiskProfileDTO> getRiskProfile(@PathVariable Long memberId) {
        RiskProfileDTO profile = memberService.getMemberRiskProfile(memberId);
        return ResponseEntity.ok(profile);
    }
}
