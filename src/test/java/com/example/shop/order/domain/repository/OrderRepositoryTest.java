package com.example.shop.order.domain.repository;

import com.example.shop.common.infrastructure.config.jpa.JpaAuditConfig;
import com.example.shop.common.infrastructure.config.jpa.QuerydslConfig;
import com.example.shop.common.infrastructure.config.jpa.audit.CustomAuditAware;
import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditConfig.class, CustomAuditAware.class, QuerydslConfig.class})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("사용자별 주문 목록을 조회할 수 있다")
    void findByUserIdReturnsPage() {
        UserEntity userEntity = createUser("order-user");
        testEntityManager.persistAndFlush(userEntity);

        OrderEntity orderEntity = OrderEntity.builder()
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .build();
        orderRepository.save(orderEntity);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = orderRepository.findByUser_Id(userEntity.getId(), pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUser().getId()).isEqualTo(userEntity.getId());
    }

    private UserEntity createUser(String username) {
        UserEntity userEntity = UserEntity.builder()
                .username(username)
                .password("password123")
                .nickname("nick-" + username)
                .email(username + "@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder()
                .role(UserRoleEntity.Role.USER)
                .build());
        return userEntity;
    }
}
