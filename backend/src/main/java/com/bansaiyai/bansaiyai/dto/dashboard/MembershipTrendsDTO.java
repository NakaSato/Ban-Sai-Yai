package com.bansaiyai.bansaiyai.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipTrendsDTO {
    private List<String> labels;
    private List<Long> newMembers;
    private List<Long> totalMembers;
}
