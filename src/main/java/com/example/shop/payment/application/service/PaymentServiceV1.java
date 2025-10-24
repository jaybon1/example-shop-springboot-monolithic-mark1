package com.example.shop.payment.application.service;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.payment.domain.entity.PaymentEntity;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceV1 {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ResGetPaymentDtoV1 getPayment(UUID paymentId, UUID authUserId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_NOT_FOUND));

        if (!paymentEntity.getUser().getId().equals(authUserId)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }

        return ResGetPaymentDtoV1.of(paymentEntity);
    }

    @Transactional
    public ResPostPaymentsDtoV1 postPayments(UUID authUserId, ReqPostPaymentsDtoV1 reqDto) {
        ReqPostPaymentsDtoV1.Payment reqPayment = reqDto.getPayment();
        UUID orderId = reqPayment.getOrderId();
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_ORDER_NOT_FOUND));

        if (!orderEntity.getUser().getId().equals(authUserId)) {
            throw new PaymentException(PaymentError.PAYMENT_ORDER_FORBIDDEN);
        }

        if (OrderEntity.Status.CANCELLED.equals(orderEntity.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ORDER_CANCELLED);
        }

        if (OrderEntity.Status.PAID.equals(orderEntity.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_EXISTS);
        }

        if (orderEntity.getPayment() != null) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_EXISTS);
        }

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .order(orderEntity)
                .user(findUser(authUserId))
                .method(reqPayment.getMethod())
                .amount(orderEntity.getTotalAmount())
                .transactionKey(reqPayment.getTransactionKey())
                .build();

        orderEntity.assignPayment(paymentEntity);
        paymentEntity.markCompleted();
        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        orderEntity.markPaid();
        return ResPostPaymentsDtoV1.of(savedPaymentEntity, orderEntity);
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_USER_NOT_FOUND));
    }
}
