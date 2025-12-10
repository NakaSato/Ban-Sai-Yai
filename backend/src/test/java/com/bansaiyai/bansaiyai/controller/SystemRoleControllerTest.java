package com.bansaiyai.bansaiyai.controller;

import com.bansaiyai.bansaiyai.BaseIntegrationTest;
import com.bansaiyai.bansaiyai.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SystemRoleControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "PRESIDENT")
    void getAllRoles_ShouldReturnRoles_WhenPresident() throws Exception {
        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @WithMockUser(roles = "MEMBER") // Member should not access this
    void getAllRoles_ShouldReturnForbidden_WhenMember() throws Exception {
        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PRESIDENT")
    void getRoleHierarchy_ShouldReturnHierarchy_WhenPresident() throws Exception {
        mockMvc.perform(get("/api/roles/hierarchy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hierarchy").isArray());
    }

    @Test
    void getAllRoles_ShouldReturnUnauthorized_WhenNoUser() throws Exception {
        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
