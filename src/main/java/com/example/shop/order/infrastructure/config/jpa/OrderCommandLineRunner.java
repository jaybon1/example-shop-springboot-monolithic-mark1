package com.example.shop.order.infrastructure.config.jpa;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.entity.OrderItemEntity;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.product.domain.entity.ProductEntity;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class OrderCommandLineRunner implements CommandLineRunner {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }

        List<ProductEntity> products = productRepository.findAll();
        if (products.isEmpty()) {
            return;
        }

        List<UserEntity> userList = userRepository.findAll();
        if (userList.isEmpty()) {
            return;
        }

        UserEntity firstUser = userList.get(0);
        ProductEntity firstProduct = products.get(0);
        savePaidOrder(firstUser, firstProduct, 2L);

        ProductEntity lastProduct = products.get(products.size() - 1);
        UserEntity secondUser = userList.size() > 1 ? userList.get(1) : firstUser;
        savePaidOrder(secondUser, lastProduct, 1L);
    }

    private void savePaidOrder(UserEntity userEntity, ProductEntity productEntity, long quantity) {
        OrderEntity orderEntity = OrderEntity.builder()
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .build();

        long unitPrice = Optional.ofNullable(productEntity.getPrice()).orElse(0L);
        long lineTotal = unitPrice * quantity;

        OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                .productId(productEntity.getId())
                .productName(productEntity.getName())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .build();

        orderEntity.addOrderItem(orderItemEntity);
        orderEntity.updateTotalAmount(lineTotal);
        orderRepository.save(orderEntity);
    }
}
