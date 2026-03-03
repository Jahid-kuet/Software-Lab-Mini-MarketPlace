package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.dto.ApiResponse;
import com.jahid.minimarketplace.dto.OrderDTO;
import com.jahid.minimarketplace.dto.ProductDTO;
import com.jahid.minimarketplace.dto.UserDTO;
import com.jahid.minimarketplace.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ===== Admin Dashboard =====
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    // ===== Admin: List all users =====
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    // ===== Admin: Toggle user enabled/disabled =====
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        adminService.toggleUserEnabled(id);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated.");
        return "redirect:/admin/users";
    }

    // ===== Admin: List all products =====
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", adminService.getAllProducts());
        return "admin/products";
    }

    // ===== Admin: Remove product =====
    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        adminService.removeProduct(id);
        redirectAttributes.addFlashAttribute("successMessage", "Product removed.");
        return "redirect:/admin/products";
    }

    // ===== Admin: List all orders =====
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", adminService.getAllOrders());
        return "admin/orders";
    }

    // ===== Admin: Update order status =====
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        adminService.updateOrderStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "Order #" + id + " status updated to " + status);
        return "redirect:/admin/orders";
    }

    // ===== REST API: GET all users =====
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsersApi() {
        return ResponseEntity.ok(ApiResponse.success("Users retrieved",
                adminService.getAllUsers()));
    }

    // ===== REST API: GET all products =====
    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProductsApi() {
        return ResponseEntity.ok(ApiResponse.success("Products retrieved",
                adminService.getAllProducts()));
    }

    // ===== REST API: GET all orders =====
    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getAllOrdersApi() {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
                adminService.getAllOrders()));
    }

    // ===== REST API: PATCH update order status =====
    @PatchMapping("/api/orders/{id}/status")
    @ResponseBody
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatusApi(
            @PathVariable Long id,
            @RequestParam String status) {
        OrderDTO updated = adminService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", updated));
    }

    // ===== REST API: DELETE product =====
    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteProductApi(@PathVariable Long id) {
        adminService.removeProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product removed successfully"));
    }

    // ===== REST API: Dashboard stats =====
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<ApiResponse<AdminService.DashboardStats>> getStatsApi() {
        return ResponseEntity.ok(ApiResponse.success("Stats retrieved",
                adminService.getDashboardStats()));
    }
}
