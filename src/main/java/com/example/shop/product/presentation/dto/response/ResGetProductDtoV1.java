package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetProductDtoV1 {

    private ProductDto product;

    public static ResGetProductDtoV1 of(ProductEntity productEntity) {
        return ResGetProductDtoV1.builder()
                .product(ProductDto.from(productEntity))
                .build();
    }

    @Getter
    @Builder
    public static class ProductDto {

        private String id;

        private String name;

        private Long price;

        private Long stock;

        public static ProductDto from(ProductEntity productEntity) {
            return ProductDto.builder()
                    .id(productEntity.getId().toString())
                    .name(productEntity.getName())
                    .price(productEntity.getPrice())
                    .stock(productEntity.getStock())
                    .build();
        }

    }

}
