package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.dto.ApiResponse;
import com.jahid.minimarketplace.dto.OrderDTO;
import com.jahid.minimarketplace.dto.OrderRequest;
import com.jahid.minimarketplace.service.OrderService;
import com.jahid.minimarketplace.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    // ===== Buyer: View my orders =====
    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public String myOrders(Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("orders",
                orderService.getMyOrders(userDetails.getUsername()));
        return "order/list";
    }

    // ===== Buyer: View single order =====
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public String viewOrder(@PathVariable Long id, Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("order",
                orderService.getOrderById(id, userDetails.getUsername()));
        return "order/detail";
    }

    // ===== Buyer: Show checkout/place order form =====
    @GetMapping("/checkout")
    @PreAuthorize("hasRole('BUYER')")
    public String checkoutForm(Model model) {
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("orderRequest", new OrderRequest());
        return "order/checkout";
    }

    // ===== Buyer: Handle place order =====
    @PostMapping("/place")
    @PreAuthorize("hasRole('BUYER')")
    public String placeOrder(@Valid @ModelAttribute("orderRequest") OrderRequest request,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.getAllActiveProducts());
            return "order/checkout";
        }
        try {
            OrderDTO order = orderService.placeOrder(request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order ID: " + order.getId());
            return "redirect:/orders";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productService.getAllActiveProducts());
            return "order/checkout";
        }
    }

    // ===== REST API: POST place order =====
    @PostMapping("/api")
    @ResponseBody
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<OrderDTO>> placeOrderApi(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO order = orderService.placeOrder(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    // ===== REST API: GET my orders =====
    @GetMapping("/api/my")
    @ResponseBody
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrdersApi(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getMyOrders(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    // ===== REST API: GET order by id =====
    @GetMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasAnyRole('BUYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderApi(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO order = orderService.getOrderById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Order retrieved", order));
    }
}
