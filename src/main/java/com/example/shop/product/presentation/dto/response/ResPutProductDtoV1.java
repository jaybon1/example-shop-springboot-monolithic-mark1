package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPutProductDtoV1 {

    private Product product;

    public static ResPutProductDtoV1 of(ProductEntity productEntity) {
        return ResPutProductDtoV1.builder()
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
