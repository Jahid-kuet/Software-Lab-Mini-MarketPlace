package com.jahid.minimarketplace.service;

import com.jahid.minimarketplace.dto.OrderDTO;
import com.jahid.minimarketplace.dto.OrderRequest;
import com.jahid.minimarketplace.entity.Order;
import com.jahid.minimarketplace.entity.OrderItem;
import com.jahid.minimarketplace.entity.Product;
import com.jahid.minimarketplace.entity.User;
import com.jahid.minimarketplace.exception.InsufficientStockException;
import com.jahid.minimarketplace.exception.ResourceNotFoundException;
import com.jahid.minimarketplace.repository.OrderRepository;
import com.jahid.minimarketplace.repository.ProductRepository;
import com.jahid.minimarketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderDTO placeOrder(OrderRequest request, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + buyerUsername));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .buyer(buyer)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemReq.getProductId()));

            if (!product.isActive()) {
                throw new ResourceNotFoundException("Product is not available: " + product.getName());
            }

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity()
                        + ", Requested: " + itemReq.getQuantity());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            orderItems.add(item);
            totalAmount = totalAmount.add(product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        return mapToDTO(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders(String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found: " + buyerUsername));
        return orderRepository.findByBuyerId(buyer.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        boolean isAdmin = userRepository.findByUsername(username)
                .map(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN")))
                .orElse(false);

        if (!isAdmin && !order.getBuyer().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to view this order");
        }

        return mapToDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return mapToDTO(orderRepository.save(order));
    }

    // ===== Mapper =====
    public OrderDTO mapToDTO(Order order) {
        List<OrderDTO.OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderDTO.OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .buyerUsername(order.getBuyer().getUsername())
                .createdAt(order.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}
