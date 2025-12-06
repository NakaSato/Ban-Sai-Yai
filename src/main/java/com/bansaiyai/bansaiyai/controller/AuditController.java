package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.dto.AuditLogDTO;
import com.bansaiyai.bansaiyai.dto.CriticalActionDTO;
import com.bansaiyai.bansaiyai.dto.ActivityHeatmapDTO;
import com.bansaiyai.bansaiyai.dto.SecurityAlertDTO;
import com.bansaiyai.bansaiyai.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for audit dashboard endpoints.
 * Provides security monitoring and activity analysis for Presidents.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuditController {

  private final AuditService auditService;

  /**
   * Get critical actions (last 10 DELETE/OVERRIDE operations)
   */
  @GetMapping("/critical-actions")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<List<CriticalActionDTO>> getCriticalActions() {
    log.info("President requested critical actions audit log");
    List<CriticalActionDTO> criticalActions = auditService.getCriticalActions();
    return ResponseEntity.ok(criticalActions);
  }

  /**
   * Get role violations (403 access denied errors)
   */
  @GetMapping("/role-violations")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<List<AuditLogDTO>> getRoleViolations() {
    log.info("President requested role violations audit log");
    List<AuditLogDTO> violations = auditService.getRoleViolations();
    return ResponseEntity.ok(violations);
  }

  /**
   * Get activity heatmap data
   */
  @GetMapping("/activity-heatmap")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<List<ActivityHeatmapDTO>> getActivityHeatmap(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    log.info("President requested activity heatmap for period: {} to {}", startDate, endDate);
    List<ActivityHeatmapDTO> heatmap = auditService.getActivityHeatmap(startDate, endDate);
    return ResponseEntity.ok(heatmap);
  }

  /**
   * Get off-hours security alerts
   */
  @GetMapping("/off-hours-alerts")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<List<SecurityAlertDTO>> getOffHoursAlerts(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    log.info("President requested off-hours security alerts for period: {} to {}", startDate, endDate);
    List<SecurityAlertDTO> alerts = auditService.getOffHoursAlerts(startDate, endDate);
    return ResponseEntity.ok(alerts);
  }

  /**
   * Get comprehensive audit summary for dashboard
   */
  @GetMapping("/summary")
  @PreAuthorize("hasRole('ROLE_PRESIDENT')")
  public ResponseEntity<?> getAuditSummary(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    log.info("President requested audit summary for period: {} to {}", startDate, endDate);

    var summary = new Object() {
      public final List<CriticalActionDTO> criticalActions = auditService.getCriticalActions();
      public final List<AuditLogDTO> roleViolations = auditService.getRoleViolations();
      public final List<ActivityHeatmapDTO> activityHeatmap = auditService.getActivityHeatmap(startDate, endDate);
      public final List<SecurityAlertDTO> offHoursAlerts = auditService.getOffHoursAlerts(startDate, endDate);
      public final long totalAudits = auditService.getTotalAuditCount(startDate, endDate);
      public final long criticalCount = auditService.getCriticalActionCount(startDate, endDate);
      public final long violationCount = auditService.getRoleViolationCount(startDate, endDate);
    };

    return ResponseEntity.ok(summary);
  }
}
