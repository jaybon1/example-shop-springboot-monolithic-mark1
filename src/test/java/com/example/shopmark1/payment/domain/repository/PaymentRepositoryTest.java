package com.example.shopmark1.payment.domain.repository;

import com.example.shopmark1.global.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shopmark1.global.infrastructure.config.jpa.QuerydslConfig;
import com.example.shopmark1.global.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shopmark1.order.domain.entity.OrderEntity;
import com.example.shopmark1.payment.domain.entity.PaymentEntity;
import com.example.shopmark1.user.domain.entity.UserEntity;
import com.example.shopmark1.user.domain.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class})
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("결제를 저장하고 다시 조회할 수 있다")
    void saveAndFindById() {
        PaymentEntity paymentEntity = paymentRepository.save(createPayment());

        PaymentEntity found = paymentRepository.findById(paymentEntity.getId()).orElseThrow();

        assertThat(found.getAmount()).isEqualTo(paymentEntity.getAmount());
        assertThat(found.getOrder().getId()).isEqualTo(paymentEntity.getOrder().getId());
        assertThat(found.getUser().getId()).isEqualTo(paymentEntity.getUser().getId());
    }

    private PaymentEntity createPayment() {
        UserEntity userEntity = UserEntity.builder()
                .username("payment-user")
                .password("payment-pass")
                .nickname("pay")
                .email("pay@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder().role(UserRoleEntity.Role.USER).build());
        testEntityManager.persist(userEntity);

        OrderEntity orderEntity = OrderEntity.builder()
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .build();
        testEntityManager.persist(orderEntity);
        testEntityManager.flush();

        return PaymentEntity.builder()
                .order(orderEntity)
                .user(userEntity)
                .method(PaymentEntity.Method.CARD)
                .amount(1000L)
                .transactionKey("tx-12345")
                .build();
    }
}
