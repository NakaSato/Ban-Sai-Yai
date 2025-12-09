package com.bansaiyai.bansaiyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for activity heatmap data visualization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityHeatmapDTO {
  private String hour;
  private String dayOfWeek;
  private int actionCount;
  private String activityLevel; // LOW, MEDIUM, HIGH
  private String period; // "OFF_HOURS" or "BUSINESS_HOURS"
}
