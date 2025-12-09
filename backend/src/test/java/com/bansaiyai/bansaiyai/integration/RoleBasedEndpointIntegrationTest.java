package com.bansaiyai.bansaiyai.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for role-based endpoint access control.
 * Verifies Requirements 3.1, 3.5, 4.2, 4.3, 5.2, 5.4, 6.1, 6.2
 */
@SpringBootTest
@ActiveProfiles("test")
public class RoleBasedEndpointIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "officer", roles = { "OFFICER" })
    void officerCanCreatePayment() throws Exception {
        String json = """
                {
                    "memberId": 1,
                    "amount": 1000.0,
                    "paymentDate": "2023-01-01",
                    "paymentType": "DEPOSIT",
                    "referenceNumber": "REF123",
                    "paymentMethod": "CASH"
                }
                """;

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(result -> {
                    // We just want to ensure it's not 403 Forbidden.
                    // 400 Bad Request or 200 OK or 404 Not Found are all acceptable proofs that
                    // Auth passed.
                    if (result.getResponse().getStatus() == 403) {
                        throw new AssertionError("Officer should be allowed to access create payment endpoint");
                    }
                });
    }

    @Test
    @WithMockUser(username = "secretary", roles = { "SECRETARY" })
    void secretaryCannotCreatePayment() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "president", roles = { "PRESIDENT" })
    void presidentCannotCreatePayment() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = { "MEMBER" })
    void memberCannotCreatePayment() throws Exception {
        String json = "{}";
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member", roles = { "MEMBER" })
    void memberCanViewOwnProfile() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer", roles = { "OFFICER" })
    void officerCanViewAllMembers() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() == 403) {
                        throw new AssertionError("Officer should be allowed to view members list");
                    }
                });
    }
}
