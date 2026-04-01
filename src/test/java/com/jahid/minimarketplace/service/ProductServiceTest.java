package com.jahid.minimarketplace.service;

import com.jahid.minimarketplace.dto.ProductDTO;
import com.jahid.minimarketplace.dto.ProductRequest;
import com.jahid.minimarketplace.entity.Product;
import com.jahid.minimarketplace.entity.User;
import com.jahid.minimarketplace.exception.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        seller = new User();
        seller.setId(1L);
        seller.setUsername("testSeller");

        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(BigDecimal.valueOf(1000))
                .stockQuantity(10)
                .category("Electronics")
                .active(true)
                .seller(seller)
                .build();

        productRequest = new ProductRequest();
        productRequest.setName("Laptop");
        productRequest.setDescription("Gaming Laptop");
        productRequest.setPrice(BigDecimal.valueOf(1000));
        productRequest.setStockQuantity(10);
        productRequest.setCategory("Electronics");
    }

    @Test
    void createProduct_Success() {
        when(userRepository.findByUsername("testSeller")).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDTO result = productService.createProduct(productRequest, "testSeller");

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals("testSeller", result.getSellerUsername());
        verify(userRepository, times(1)).findByUsername("testSeller");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_SellerNotFound() {
        when(userRepository.findByUsername("unknownSeller")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(productRequest, "unknownSeller"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllActiveProducts_Success() {
        when(productRepository.findByActiveTrue()).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.getAllActiveProducts();

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        verify(productRepository, times(1)).findByActiveTrue();
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_NotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void getProductsByCategory_Success() {
        when(productRepository.findByCategoryIgnoreCaseAndActiveTrue("Electronics")).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.getProductsByCategory("Electronics");

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByCategoryIgnoreCaseAndActiveTrue("Electronics");
    }

    @Test
    void searchProducts_Success() {
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Laptop")).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.searchProducts("Laptop");

        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByNameContainingIgnoreCaseAndActiveTrue("Laptop");
    }

    @Test
    void getMyProducts_Success() {
        when(userRepository.findByUsername("testSeller")).thenReturn(Optional.of(seller));
        when(productRepository.findBySellerId(1L)).thenReturn(Collections.singletonList(product));

        List<ProductDTO> result = productService.getMyProducts("testSeller");

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findByUsername("testSeller");
        verify(productRepository, times(1)).findBySellerId(1L);
    }

    @Test
    void updateProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productRequest.setName("Updated Laptop");
        ProductDTO result = productService.updateProduct(1L, productRequest, "testSeller");

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void updateProduct_Unauthorized() {
        User otherSeller = new User();
        otherSeller.setUsername("otherSeller");
        product.setSeller(otherSeller);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(AccessDeniedException.class, () -> productService.updateProduct(1L, productRequest, "testSeller"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1L, "testSeller");

        assertFalse(product.isActive());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void adminDeleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.adminDeleteProduct(1L);

        assertFalse(product.isActive());
        verify(productRepository, times(1)).save(product);
    }
}
