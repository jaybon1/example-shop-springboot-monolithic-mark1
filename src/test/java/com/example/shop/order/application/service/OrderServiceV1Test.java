package com.example.shop.order.application.service;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.payment.domain.entity.PaymentEntity;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.product.domain.entity.ProductEntity;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import com.example.shop.user.domain.repository.UserRepository;
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
class OrderServiceV1Test {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceV1 orderServiceV1;

    private UserEntity userEntity;
    private ProductEntity productEntity;
    private OrderEntity existingOrder;

    @BeforeEach
    void setUp() {
        userEntity = createUser(UUID.randomUUID(), "order-user", UserRoleEntity.Role.USER);
        productEntity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("sample-product")
                .price(1000L)
                .stock(10L)
                .build();
        existingOrder = OrderEntity.builder()
                .id(UUID.randomUUID())
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .build();
    }

    @Test
    @DisplayName("사용자는 자신의 주문 목록만 페이지로 조회할 수 있다")
    void getOrdersForSelf() {
        Pageable pageable = PageRequest.of(0, 5);
        when(orderRepository.findByUser_Id(userEntity.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(existingOrder)));

        var response = orderServiceV1.getOrders(userEntity.getId(), List.of(UserRoleEntity.Role.USER.toString()), pageable);

        assertThat(response.getOrderPage().getContent()).hasSize(1);
        verify(orderRepository).findByUser_Id(userEntity.getId(), pageable);
    }

    @Test
    @DisplayName("관리자는 모든 주문 목록을 조회할 수 있다")
    void getOrdersAsAdmin() {
        Pageable pageable = PageRequest.of(0, 5);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(existingOrder)));

        var response = orderServiceV1.getOrders(UUID.randomUUID(), List.of(UserRoleEntity.Role.ADMIN.toString()), pageable);

        assertThat(response.getOrderPage().getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("주문 생성 시 상품 재고 차감 및 총액 계산이 수행된다")
    void postOrdersUpdatesStockAndTotalAmount() {
        when(productRepository.findAllById(any())).thenReturn(List.of(productEntity));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity savedOrder = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedOrder, "id", UUID.randomUUID());
            savedOrder.getOrderItemList()
                    .forEach(orderItem -> ReflectionTestUtils.setField(orderItem, "id", UUID.randomUUID()));
            return savedOrder;
        });
        when(userRepository.findDefaultById(userEntity.getId())).thenReturn(userEntity);

        ReqPostOrdersDtoV1 reqDto = ReqPostOrdersDtoV1.builder()
                .order(ReqPostOrdersDtoV1.OrderDto.builder()
                        .orderItemList(List.of(
                                ReqPostOrdersDtoV1.OrderDto.OrderItem.builder()
                                        .productId(productEntity.getId())
                                        .quantity(2L)
                                        .build()
                        ))
                        .build())
                .build();

        ResPostOrdersDtoV1 response = orderServiceV1.postOrders(userEntity.getId(), reqDto);

        assertThat(response.getOrder().getTotalAmount()).isEqualTo(2000L);
        assertThat(productEntity.getStock()).isEqualTo(8L);
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("주문 취소는 사용자 본인 또는 관리자/매니저만 가능하다")
    void postOrderCancelWithAuthority() {
        existingOrder.assignPayment(PaymentEntity.builder()
                .user(userEntity)
                .order(existingOrder)
                .method(PaymentEntity.Method.CARD)
                .amount(1500L)
                .build());
        existingOrder.markPaid();
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        orderServiceV1.postOrderCancel(userEntity.getId(), List.of(UserRoleEntity.Role.USER.toString()), existingOrder.getId());

        assertThat(existingOrder.getStatus()).isEqualTo(OrderEntity.Status.CANCELLED);
    }

    @Test
    @DisplayName("주문 취소 시 권한이 없으면 예외를 던진다")
    void postOrderCancelForbidden() {
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        assertThatThrownBy(() -> orderServiceV1.postOrderCancel(UUID.randomUUID(), List.of("USER"), existingOrder.getId()))
                .isInstanceOf(OrderException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(OrderError.ORDER_FORBIDDEN.getErrorMessage());
    }

    @Test
    @DisplayName("환불 시 결제를 찾지 못하면 예외를 던진다")
    void postOrderCancelPaymentMissing() {
        existingOrder.markPaid();
        when(orderRepository.findById(existingOrder.getId())).thenReturn(Optional.of(existingOrder));

        assertThatThrownBy(() -> orderServiceV1.postOrderCancel(userEntity.getId(), List.of(UserRoleEntity.Role.USER.toString()), existingOrder.getId()))
                .isInstanceOf(PaymentException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(PaymentError.PAYMENT_NOT_FOUND.getErrorMessage());
    }

    private UserEntity createUser(UUID id, String username, UserRoleEntity.Role role) {
        UserEntity userEntity = UserEntity.builder()
                .id(id)
                .username(username)
                .password("secure-password")
                .nickname("nick-" + username)
                .email(username + "@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder().role(role).build());
        return userEntity;
    }
}
