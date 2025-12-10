package com.bansaiyai.bansaiyai.component;

import com.bansaiyai.bansaiyai.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ProductionReadinessTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private NotificationService notificationService;

    @Test
    public void testActuatorBeansPresent() {
        // Verify Actuator endpoint beans are loaded
        assertThat(context.containsBean("healthEndpoint")).isTrue();
        assertThat(context.containsBean("infoEndpoint")).isTrue();
    }

    @Test
    public void testNotificationMock() {
        // Just verify the service is loadable and runs without error in test context
        // (emailEnabled is false by default in test/dev)
        notificationService.sendNotification("test@test.com", "Subject", "Body");
    }
}
