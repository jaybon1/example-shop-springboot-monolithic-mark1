package com.example.shopmark1.product.domain.repository;

import com.example.shopmark1.product.domain.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Optional<ProductEntity> findByName(String name);

    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
