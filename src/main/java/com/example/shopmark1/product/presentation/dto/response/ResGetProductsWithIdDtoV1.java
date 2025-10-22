package com.example.shopmark1.product.presentation.dto.response;

import com.example.shopmark1.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetProductsWithIdDtoV1 {

    private Product product;

    public static ResGetProductsWithIdDtoV1 of(ProductEntity productEntity) {
        return ResGetProductsWithIdDtoV1.builder()
                .product(Product.from(productEntity))
                .build();
    }

    @Getter
    @Builder
    public static class Product {

        private String id;

        private String name;

        private Long price;

        private Long stock;

        public static Product from(ProductEntity productEntity) {
            return Product.builder()
                    .id(productEntity.getId().toString())
                    .name(productEntity.getName())
                    .price(productEntity.getPrice())
                    .stock(productEntity.getStock())
                    .build();
        }

    }

}
