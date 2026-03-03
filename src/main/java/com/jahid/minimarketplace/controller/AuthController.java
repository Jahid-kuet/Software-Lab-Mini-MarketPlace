package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.dto.ApiResponse;
import com.jahid.minimarketplace.dto.RegisterRequest;
import com.jahid.minimarketplace.dto.UserDTO;
import com.jahid.minimarketplace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // ===== Show Login Page =====
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // ===== Show Register Page =====
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    // ===== Handle Registration (Form submit) =====
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }

    // ===== REST: Register (API) =====
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<ApiResponse<UserDTO>> registerApi(
            @Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userDTO));
    }
}
