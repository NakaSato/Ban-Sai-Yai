package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.SystemConfig;
import com.bansaiyai.bansaiyai.repository.SystemConfigRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import com.bansaiyai.bansaiyai.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @jakarta.annotation.PostConstruct
    public void initDefaults() {
        log.info("Checking System Configurations...");
        initializeConfig("LOAN_INTEREST_RATE_BUSINESS", "12.0", "Annual Interest Rate for Business Loans (%)");
        initializeConfig("LOAN_INTEREST_RATE_EDUCATION", "8.0", "Annual Interest Rate for Education Loans (%)");
        initializeConfig("LOAN_INTEREST_RATE_HOUSING", "10.0", "Annual Interest Rate for Housing Loans (%)");
        initializeConfig("LOAN_INTEREST_RATE_EMERGENCY", "15.0", "Annual Interest Rate for Emergency Loans (%)");
        initializeConfig("LOAN_INTEREST_RATE_PERSONAL", "11.0", "Annual Interest Rate for Personal Loans (%)");
        initializeConfig("LOAN_INTEREST_RATE_GENERAL", "10.0", "Default Annual Interest Rate (%)");
    }

    private void initializeConfig(String key, String defaultValue, String description) {
        if (!systemConfigRepository.existsById(key)) {
            log.info("Seeding default config: {} = {}", key, defaultValue);
            systemConfigRepository.save(SystemConfig.builder()
                    .configKey(key)
                    .configValue(defaultValue)
                    .description(description)
                    .updatedBy("SYSTEM_INIT")
                    .build());
        }
    }

    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Transactional
    public SystemConfig updateConfig(String key, String value, String description, String username) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElse(SystemConfig.builder()
                        .configKey(key)
                        .build());

        config.setConfigValue(value);
        if (description != null) {
            config.setDescription(description);
        }
        config.setUpdatedBy(username);

        log.info("System Config updated: {} = {} by {}", key, value, username);
        log.info("System Config updated: {} = {} by {}", key, value, username);

        SystemConfig savedConfig = systemConfigRepository.save(config);

        // Audit Log
        try {
            User adminUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Admin user not found: " + username));

            auditService.logAction(adminUser, "CONFIG_UPDATE", "SystemConfig",
                    null, // No ID for config key usually, or use hash? Using key as description in
                          // newValues is better.
                    "Key: " + key,
                    "Value: " + value + ", Desc: " + description);
        } catch (Exception e) {
            log.error("Failed to audit config update", e);
        }

        return savedConfig;
    }

    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> new BigDecimal(config.getConfigValue()))
                .orElse(defaultValue);
    }
}
