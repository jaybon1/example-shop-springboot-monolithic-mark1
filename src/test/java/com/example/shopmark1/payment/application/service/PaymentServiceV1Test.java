package com.example.shopmark1.payment.application.service;

import com.example.shopmark1.order.domain.entity.OrderEntity;
import com.example.shopmark1.order.domain.repository.OrderRepository;
import com.example.shopmark1.payment.domain.entity.PaymentEntity;
import com.example.shopmark1.payment.domain.repository.PaymentRepository;
import com.example.shopmark1.payment.presentation.advice.PaymentError;
import com.example.shopmark1.payment.presentation.advice.PaymentException;
import com.example.shopmark1.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shopmark1.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.example.shopmark1.user.domain.entity.UserEntity;
import com.example.shopmark1.user.domain.entity.UserRoleEntity;
import com.example.shopmark1.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceV1Test {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceV1 paymentServiceV1;

    private UserEntity userEntity;
    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("pay-user")
                .password("pay-pass")
                .nickname("pay-nick")
                .email("pay@example.com")
                .build();
        userEntity.add(UserRoleEntity.builder().role(UserRoleEntity.Role.USER).build());

        orderEntity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .user(userEntity)
                .status(OrderEntity.Status.CREATED)
                .build();
        orderEntity.updateTotalAmount(3000L);
    }

    @Test
    @DisplayName("결제를 생성하면 주문 상태가 PAID로 변경된다")
    void postPaymentsMarksOrderPaid() {
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity paymentEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(paymentEntity, "id", UUID.randomUUID());
            return paymentEntity;
        });

        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.Payment.builder()
                        .orderId(orderEntity.getId())
                        .method(PaymentEntity.Method.CARD)
                        .transactionKey("tx-key")
                        .build())
                .build();

        ResPostPaymentsDtoV1 response = paymentServiceV1.postPayments(userEntity.getId(), reqDto);

        assertThat(response.getPayment().getOrderStatus()).isEqualTo(OrderEntity.Status.PAID);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("다른 사용자의 주문을 결제하려면 예외가 발생한다")
    void postPaymentsForbiddenForDifferentUser() {
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));

        ReqPostPaymentsDtoV1 reqDto = ReqPostPaymentsDtoV1.builder()
                .payment(ReqPostPaymentsDtoV1.Payment.builder()
                        .orderId(orderEntity.getId())
                        .method(PaymentEntity.Method.CARD)
                        .transactionKey("tx-key")
                        .build())
                .build();

        assertThatThrownBy(() -> paymentServiceV1.postPayments(UUID.randomUUID(), reqDto))
                .isInstanceOf(PaymentException.class)
                .extracting(Throwable::getMessage)
                .asString()
                .contains(PaymentError.PAYMENT_ORDER_FORBIDDEN.getErrorMessage());
    }
}
