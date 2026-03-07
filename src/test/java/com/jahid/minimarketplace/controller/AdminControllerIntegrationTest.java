package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    // IT-1: Unauthenticated access to admin dashboard redirects to login
    @Test
    public void testAdminDashboard_WithoutAuth_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // IT-2: Admin-authenticated user can access dashboard
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testAdminDashboard_WithAdminAuth_ShouldReturnDashboard() throws Exception {
        when(adminService.getDashboardStats()).thenReturn(
                AdminService.DashboardStats.builder()
                        .totalUsers(5)
                        .totalProducts(10)
                        .totalOrders(3)
                        .activeProducts(8)
                        .build());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats"));
    }
}
