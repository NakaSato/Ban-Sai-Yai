package com.bansaiyai.bansaiyai.service;

import com.bansaiyai.bansaiyai.entity.SystemConfig;
import com.bansaiyai.bansaiyai.entity.User;
import com.bansaiyai.bansaiyai.repository.SystemConfigRepository;
import com.bansaiyai.bansaiyai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemConfigServiceTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private SystemConfigService systemConfigService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void initDefaults_ShouldSeedConfigs_WhenNotExist() {
        when(systemConfigRepository.existsById(anyString())).thenReturn(false);

        systemConfigService.initDefaults();

        // Verify seeding happens for known keys (e.g., LOAN_INTEREST_RATE_BUSINESS)
        // We know there are 6 default keys
        verify(systemConfigRepository, atLeast(6)).save(any(SystemConfig.class));
    }

    @Test
    void initDefaults_ShouldSkip_WhenExists() {
        when(systemConfigRepository.existsById(anyString())).thenReturn(true);

        systemConfigService.initDefaults();

        verify(systemConfigRepository, never()).save(any(SystemConfig.class));
    }

    @Test
    void getBigDecimal_ShouldReturnConfiguredValue() {
        SystemConfig mockConfig = SystemConfig.builder()
                .configKey("TEST_KEY")
                .configValue("12.5")
                .build();

        when(systemConfigRepository.findByConfigKey("TEST_KEY")).thenReturn(Optional.of(mockConfig));

        BigDecimal result = systemConfigService.getBigDecimal("TEST_KEY", BigDecimal.TEN);

        assertEquals(new BigDecimal("12.5"), result);
    }

    @Test
    void getBigDecimal_ShouldReturnDefault_WhenMissing() {
        when(systemConfigRepository.findByConfigKey("MISSING_KEY")).thenReturn(Optional.empty());

        BigDecimal result = systemConfigService.getBigDecimal("MISSING_KEY", BigDecimal.TEN);

        assertEquals(BigDecimal.TEN, result);
    }

    @Test
    void updateConfig_ShouldUpdateAndAudit() {
        String key = "LOAN_RATE";
        String value = "5.0";
        String username = "admin";

        SystemConfig existingConfig = SystemConfig.builder().configKey(key).configValue("4.0").build();
        User adminUser = new User();
        adminUser.setUsername(username);

        when(systemConfigRepository.findByConfigKey(key)).thenReturn(Optional.of(existingConfig));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        SystemConfig updated = systemConfigService.updateConfig(key, value, "Updated Rate", username);

        assertEquals(value, updated.getConfigValue());
        assertEquals("Updated Rate", updated.getDescription());

        // Verify Audit Log
        verify(auditService).logAction(eq(adminUser), eq("CONFIG_UPDATE"), eq("SystemConfig"), isNull(), anyString(),
                anyString());
    }
}
