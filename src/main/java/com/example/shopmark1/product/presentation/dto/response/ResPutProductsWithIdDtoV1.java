package com.example.shopmark1.product.presentation.dto.response;

import com.example.shopmark1.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPutProductsWithIdDtoV1 {

    private Product product;

    public static ResPutProductsWithIdDtoV1 of(ProductEntity productEntity) {
        return ResPutProductsWithIdDtoV1.builder()
                .product(Product.from(productEntity))
                .build();
    }

    @Getter
    @Builder
    public static class Product {

        private String id;

        public static Product from(ProductEntity productEntity) {
            return Product.builder()
                    .id(productEntity.getId().toString())
                    .build();
        }

    }

}
