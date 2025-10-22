package com.example.shopmark1.product.infrastructure.config.jpa;

import com.example.shopmark1.product.domain.entity.ProductEntity;
import com.example.shopmark1.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class ProductCommandLineRunner implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            saveProductEntityBy("안성탕면 5개 번들", 3000L, 125L);
            saveProductEntityBy("LA갈비 1kg", 25000L, 95L);
            saveProductEntityBy("동원참치 1캔", 2100L, 500L);
        }
    }

    private void saveProductEntityBy(String name, Long price, Long stock) {
        ProductEntity productEntity = ProductEntity.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
        productRepository.save(productEntity);
    }

}
