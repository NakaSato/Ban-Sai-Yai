package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.entity.SystemConfig;
import com.bansaiyai.bansaiyai.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllConfigs());
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ResponseEntity<SystemConfig> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String value = request.get("value");
        String description = request.get("description");

        return ResponseEntity.ok(systemConfigService.updateConfig(key, value, description, authentication.getName()));
    }
}
