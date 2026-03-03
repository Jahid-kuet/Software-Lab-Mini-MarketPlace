package com.jahid.minimarketplace.repository;

import com.jahid.minimarketplace.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String keyword);
}
