package com.example.shop.product.application.service;

import com.example.shop.product.domain.entity.ProductEntity;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.product.presentation.advice.ProductError;
import com.example.shop.product.presentation.advice.ProductException;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPutProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResPutProductDtoV1;
import com.example.shop.user.domain.entity.UserRoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV1 {

    private final ProductRepository productRepository;

    public ResGetProductsDtoV1 getProducts(Pageable pageable, String name) {
        String normalizedName = normalize(name);

        Page<ProductEntity> productEntityPage = normalizedName == null
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
        return ResGetProductsDtoV1.builder()
                .productPage(new ResGetProductsDtoV1.ProductPage(productEntityPage))
                .build();
    }

    public ResGetProductDtoV1 getProduct(UUID productId) {
        ProductEntity productEntity = findProductById(productId);
        return ResGetProductDtoV1.of(productEntity);
    }

    @Transactional
    public ResPostProductsDtoV1 postProducts(ReqPostProductsDtoV1 reqDto) {
        ReqPostProductsDtoV1.ProductDto reqProduct = reqDto.getProduct();
        String name = reqProduct.getName().trim();
        validateDuplicatedName(name, Optional.empty());

        ProductEntity productEntity = ProductEntity.builder()
                .name(name)
                .price(reqProduct.getPrice())
                .stock(reqProduct.getStock())
                .build();

        productRepository.save(productEntity);
        return ResPostProductsDtoV1.of(productEntity);
    }

    @Transactional
    public ResPutProductDtoV1 putProduct(List<String> authUserRoleList, UUID productId, ReqPutProductDtoV1 reqDto) {
        validateWriteAuthority(authUserRoleList);
        ProductEntity productEntity = findProductById(productId);

        ReqPutProductDtoV1.ProductDto reqProduct = reqDto.getProduct();

        String nameToUpdate = null;
        if (reqProduct.getName() != null) {
            String trimmed = reqProduct.getName().trim();
            if (!trimmed.equals(productEntity.getName())) {
                validateDuplicatedName(trimmed, Optional.of(productId));
            }
            nameToUpdate = trimmed;
        }

        productEntity.update(
                nameToUpdate,
                reqProduct.getPrice(),
                reqProduct.getStock()
        );

        return ResPutProductDtoV1.of(productEntity);
    }

    @Transactional
    public void deleteProduct(UUID authUserId, List<String> authUserRoleList, UUID productId) {
        validateWriteAuthority(authUserRoleList);
        ProductEntity productEntity = findProductById(productId);
        if (authUserId == null) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }
        productEntity.markDeleted(Instant.now(), authUserId);
    }

    private ProductEntity findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductError.PRODUCT_CAN_NOT_FOUND));
    }

    private void validateDuplicatedName(String name, Optional<UUID> excludeId) {
        productRepository.findByName(name).ifPresent(productEntity -> {
            if (excludeId.isEmpty() || !productEntity.getId().equals(excludeId.get())) {
                throw new ProductException(ProductError.PRODUCT_NAME_DUPLICATED);
            }
        });
    }

    private void validateWriteAuthority(List<String> authUserRoleList) {
        if (authUserRoleList == null) {
            throw new ProductException(ProductError.PRODUCT_FORBIDDEN);
        }
        boolean hasAuthority = authUserRoleList.contains(UserRoleEntity.Role.ADMIN.toString())
                || authUserRoleList.contains(UserRoleEntity.Role.MANAGER.toString());
        if (!hasAuthority) {
            throw new ProductException(ProductError.PRODUCT_FORBIDDEN);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
