package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.entity.ProductEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@Builder
public class ResGetProductsDtoV1 {

    private ProductPage productPage;

    @Getter
    @ToString
    public static class ProductPage extends PagedModel<ProductPage.Product> {

        public ProductPage(Page<ProductEntity> productEntityPage) {
            super(
                    new PageImpl<>(
                            Product.from(productEntityPage.getContent()),
                            productEntityPage.getPageable(),
                            productEntityPage.getTotalElements()
                    )
            );
        }

        @Getter
        @Builder
        public static class Product {

            private String id;
            private String name;
            private Long price;
            private Long stock;

            private static List<Product> from(List<ProductEntity> productEntityList) {
                return productEntityList.stream()
                        .map(Product::from)
                        .toList();
            }

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

}
