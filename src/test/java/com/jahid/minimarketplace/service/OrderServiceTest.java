package com.jahid.minimarketplace.service;

import com.jahid.minimarketplace.dto.OrderDTO;
import com.jahid.minimarketplace.dto.OrderRequest;
import com.jahid.minimarketplace.entity.*;
import com.jahid.minimarketplace.exception.InsufficientStockException;
import com.jahid.minimarketplace.exception.ResourceNotFoundException;
import com.jahid.minimarketplace.repository.OrderRepository;
import com.jahid.minimarketplace.repository.ProductRepository;
import com.jahid.minimarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User buyer;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        buyer = User.builder()
                .id(1L)
                .username("testBuyer")
                .email("buyer@example.com")
                .password("encoded")
                .fullName("Test Buyer")
                .enabled(true)
                .roles(Set.of(Role.builder().id(2L).name(Role.RoleName.ROLE_BUYER).build()))
                .build();

        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(1000))
                .stockQuantity(10)
                .category("Electronics")
                .active(true)
                .seller(User.builder().id(3L).username("seller").build())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(1000))
                .build();

        order = Order.builder()
                .id(1L)
                .buyer(buyer)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(2000))
                .orderItems(new ArrayList<>(List.of(item)))
                .build();
        item.setOrder(order);
    }

    // UT-1: Successful order placement with stock deduction
    @Test
    void placeOrder_Success() {
        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(2);
        request.setItems(List.of(itemReq));

        when(userRepository.findByUsername("testBuyer")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.placeOrder(request, "testBuyer");

        assertNotNull(result);
        assertEquals("testBuyer", result.getBuyerUsername());
        assertEquals(8, product.getStockQuantity()); // 10 - 2
        verify(orderRepository).save(any(Order.class));
    }

    // UT-2: Insufficient stock throws exception
    @Test
    void placeOrder_InsufficientStock_ThrowsException() {
        product.setStockQuantity(1);
        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setProductId(1L);
        itemReq.setQuantity(5);
        request.setItems(List.of(itemReq));

        when(userRepository.findByUsername("testBuyer")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> orderService.placeOrder(request, "testBuyer"));
    }

    // UT-3: Unauthorized user cannot view another buyer's order
    @Test
    void getOrderById_UnauthorizedUser_ThrowsException() {
        User otherBuyer = User.builder()
                .id(5L).username("other")
                .roles(Set.of(Role.builder().id(2L).name(Role.RoleName.ROLE_BUYER).build()))
                .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherBuyer));

        assertThrows(AccessDeniedException.class,
                () -> orderService.getOrderById(1L, "other"));
    }
}
