package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;

    // ===== Home page — public =====
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAllActiveProducts());
        return "index";
    }

    // ===== Dashboard — role-based redirect =====
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        if (authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin";
        }
        if (authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_SELLER"))) {
            return "redirect:/products/my";
        }
        if (authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_BUYER"))) {
            return "redirect:/orders";
        }
        return "redirect:/";
    }

    // ===== Error 403 page =====
    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403";
    }
}
