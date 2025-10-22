package com.example.shopmark1.product.application.service;

import com.example.shopmark1.product.domain.entity.ProductEntity;
import com.example.shopmark1.product.domain.repository.ProductRepository;
import com.example.shopmark1.product.presentation.advice.ProductError;
import com.example.shopmark1.product.presentation.advice.ProductException;
import com.example.shopmark1.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shopmark1.product.presentation.dto.request.ReqPutProductsWithIdDtoV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceV1Test {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceV1 productServiceV1;

    private ProductEntity existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("exist-product")
                .price(1000L)
                .stock(10L)
                .build();
    }

    @Test
    @DisplayName("상품 목록을 페이지 단위로 조회할 수 있다")
    void getProductsReturnsPage() {
        Pageable pageable = PageRequest.of(0, 5);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingProduct)));

        var response = productServiceV1.getProducts(pageable, null);

        assertThat(response.getProductPage().getContent()).hasSize(1);
        assertThat(response.getProductPage().getContent().get(0).getName()).isEqualTo("exist-product");
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("상품 목록은 이름 부분 검색으로 필터링된다")
    void getProductsFiltersByName() {
        Pageable pageable = PageRequest.of(0, 5);
        when(productRepository.findByNameContainingIgnoreCase("exist", pageable))
                .thenReturn(new PageImpl<>(List.of(existingProduct)));

        var response = productServiceV1.getProducts(pageable, " exist ");

        assertThat(response.getProductPage().getContent()).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("exist", pageable);
    }

    @Test
    @DisplayName("상품 등록 시 이름 중복을 검증한다")
    void postProductsValidatesDuplicateName() {
        when(productRepository.findByName("new product")).thenReturn(Optional.empty());
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity savedProduct = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProduct, "id", UUID.randomUUID());
            return savedProduct;
        });

        ReqPostProductsDtoV1 reqDto = ReqPostProductsDtoV1.builder()
                .product(ReqPostProductsDtoV1.Product.builder()
                        .name(" new product ")
                        .price(5000L)
                        .stock(20L)
                        .build())
                .build();

        var response = productServiceV1.postProducts(reqDto);

        assertThat(response.getProduct().getId()).isNotNull();
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    @DisplayName("상품 수정 시 권한 없으면 예외를 던진다")
    void putProductsWithIdRequiresAuthority() {
        ReqPutProductsWithIdDtoV1 reqDto = ReqPutProductsWithIdDtoV1.builder()
                .product(ReqPutProductsWithIdDtoV1.Product.builder()
                        .name("updated")
                        .price(2000L)
                        .stock(5L)
                        .build())
                .build();

        assertThatThrownBy(() -> productServiceV1.putProductsWithId(UUID.randomUUID(), List.of("USER"), existingProduct.getId(), reqDto))
                .isInstanceOf(ProductException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(ProductError.PRODUCT_FORBIDDEN.getErrorMessage());
    }

    @Test
    @DisplayName("상품 삭제 시 권한과 사용자 ID를 검증한다")
    void deleteProductsWithIdChecksAuthorityAndUserId() {
        when(productRepository.findById(existingProduct.getId())).thenReturn(Optional.of(existingProduct));

        UUID deleterId = UUID.randomUUID();
        productServiceV1.deleteProductsWithId(deleterId, List.of("ADMIN"), existingProduct.getId());

        assertThat(existingProduct.getDeletedAt()).isNotNull();
        assertThat(existingProduct.getDeletedBy()).isEqualTo(deleterId.toString());
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외를 던진다")
    void deleteProductsWithIdThrowsWhenMissing() {
        when(productRepository.findById(existingProduct.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productServiceV1.deleteProductsWithId(UUID.randomUUID(), List.of("ADMIN"), existingProduct.getId()))
                .isInstanceOf(ProductException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(ProductError.PRODUCT_CAN_NOT_FOUND.getErrorMessage());
    }
}
