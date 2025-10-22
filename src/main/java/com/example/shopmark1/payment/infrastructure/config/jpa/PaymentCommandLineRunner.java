package com.example.shopmark1.payment.infrastructure.config.jpa;

import com.example.shopmark1.order.domain.entity.OrderEntity;
import com.example.shopmark1.order.domain.repository.OrderRepository;
import com.example.shopmark1.payment.domain.entity.PaymentEntity;
import com.example.shopmark1.payment.domain.repository.PaymentRepository;
import com.example.shopmark1.user.domain.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class PaymentCommandLineRunner implements CommandLineRunner {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            return;
        }

        List<OrderEntity> orderList = orderRepository.findAll();
        if (orderList.isEmpty()) {
            return;
        }

        for (int index = 0; index < orderList.size(); index++) {
            OrderEntity orderEntity = orderList.get(index);
            createCompletedPayment(orderEntity, index);
        }
    }

    private void createCompletedPayment(OrderEntity orderEntity, int seedIndex) {
        if (OrderEntity.Status.CANCELLED.equals(orderEntity.getStatus())) {
            return;
        }

        if (orderEntity.getPayment() != null) {
            return;
        }

        UserEntity userEntity = orderEntity.getUser();
        if (userEntity == null) {
            return;
        }

        Long totalAmount = Optional.ofNullable(orderEntity.getTotalAmount()).orElse(0L);
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .order(orderEntity)
                .user(userEntity)
                .method(PaymentEntity.Method.CARD)
                .amount(totalAmount)
                .transactionKey("demo-transaction-%d".formatted(seedIndex + 1))
                .build();

        orderEntity.assignPayment(paymentEntity);
        paymentEntity.markCompleted();
        paymentRepository.save(paymentEntity);

        orderEntity.markPaid();
        orderRepository.save(orderEntity);
    }
}
