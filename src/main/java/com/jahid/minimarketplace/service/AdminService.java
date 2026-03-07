package com.jahid.minimarketplace.service;

import com.jahid.minimarketplace.dto.OrderDTO;
import com.jahid.minimarketplace.dto.ProductDTO;
import com.jahid.minimarketplace.dto.UserDTO;
import com.jahid.minimarketplace.repository.OrderRepository;
import com.jahid.minimarketplace.repository.ProductRepository;
import com.jahid.minimarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userService::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleUserEnabled(Long userId) {
        userService.toggleUserEnabled(userId);
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productService::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeProduct(Long productId) {
        productService.adminDeleteProduct(productId);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderService::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @Transactional
    public void removeOrder(Long orderId) {
        orderService.deleteOrder(orderId);
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .totalProducts(productRepository.count())
                .totalOrders(orderRepository.count())
                .activeProducts(productRepository.findByActiveTrue().size())
                .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class DashboardStats {
        private long totalUsers;
        private long totalProducts;
        private long totalOrders;
        private long activeProducts;
    }
}
