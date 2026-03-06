package com.jahid.minimarketplace.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.jahid.minimarketplace.service.ProductService;
import java.util.Collections;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void testListProducts_ShouldReturnProductListView() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/list"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    public void testMyProducts_WithoutAuth_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/products/my"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testSeller", roles = {"SELLER"})
    public void testMyProducts_WithSellerAuth_ShouldReturnMyProductsView() throws Exception {
        mockMvc.perform(get("/products/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("product/my-products"))
                .andExpect(model().attributeExists("products"));
    }
}
