package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostProductsDtoV1 {

    private ProductDto product;

    public static ResPostProductsDtoV1 of(ProductEntity productEntity) {
        return ResPostProductsDtoV1.builder()
                .product(ProductDto.from(productEntity))
                .build();
    }

    @Getter
    @Builder
    public static class ProductDto {

        private String id;

        public static ProductDto from(ProductEntity productEntity) {
            return ProductDto.builder()
                    .id(productEntity.getId().toString())
                    .build();
        }

    }

}
