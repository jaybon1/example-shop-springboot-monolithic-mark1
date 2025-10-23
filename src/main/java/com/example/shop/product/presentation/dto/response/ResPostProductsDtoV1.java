package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostProductsDtoV1 {

    private Product product;

    public static ResPostProductsDtoV1 of(ProductEntity productEntity) {
        return ResPostProductsDtoV1.builder()
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
