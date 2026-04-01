package com.jahid.minimarketplace.controller;

import com.jahid.minimarketplace.dto.ApiResponse;
import com.jahid.minimarketplace.dto.ProductDTO;
import com.jahid.minimarketplace.dto.ProductRequest;
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
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ===== Public: List all active products =====
    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String category) {
        List<ProductDTO> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProducts(keyword);
        } else if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllActiveProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        return "product/list";
    }

    // ===== Public: View single product =====
    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product/detail";
    }

    // ===== Seller: Show products created by seller =====
    @GetMapping("/my")
    @PreAuthorize("hasRole('SELLER')")
    public String myProducts(Model model,
                             @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("products",
                productService.getMyProducts(userDetails.getUsername()));
        return "product/my-products";
    }

    // ===== Seller: Show create product form =====
    @GetMapping("/create")
    @PreAuthorize("hasRole('SELLER')")
    public String createProductForm(Model model) {
        model.addAttribute("productRequest", new ProductRequest());
        return "product/create";
    }

    // ===== Seller: Handle create product =====
    @PostMapping("/create")
    @PreAuthorize("hasRole('SELLER')")
    public String createProduct(@Valid @ModelAttribute("productRequest") ProductRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "product/create";
        }
        try {
            productService.createProduct(request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            return "redirect:/products/my";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "product/create";
        }
    }

    // ===== Seller: Show edit product form =====
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('SELLER')")
    public String editProductForm(@PathVariable Long id, Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        ProductDTO product = productService.getProductById(id);
        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStockQuantity(product.getStockQuantity());
        request.setCategory(product.getCategory());
        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        return "product/edit";
    }

    // ===== Seller: Handle update product =====
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('SELLER')")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("productRequest") ProductRequest request,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            return "product/edit";
        }
        try {
            productService.updateProduct(id, request, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/products/my";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productId", id);
            return "product/edit";
        }
    }

    // ===== Seller: Delete product =====
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('SELLER')")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        return "redirect:/products/my";
    }

    // ===== REST API: GET all active products =====
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProductsApi(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        List<ProductDTO> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProducts(keyword);
        } else if (category != null && !category.isBlank()) {
            products = productService.getProductsByCategory(category);
        } else {
            products = productService.getAllActiveProducts();
        }
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    // ===== REST API: GET product by id =====
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<ProductDTO>> getProductApi(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved",
                productService.getProductById(id)));
    }

    // ===== REST API: POST create product =====
    @PostMapping("/api")
    @ResponseBody
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProductApi(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductDTO created = productService.createProduct(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    // ===== REST API: PUT update product =====
    @PutMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProductApi(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProductDTO updated = productService.updateProduct(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    // ===== REST API: DELETE product =====
    @DeleteMapping("/api/{id}")
    @ResponseBody
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProductApi(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.deleteProduct(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}
