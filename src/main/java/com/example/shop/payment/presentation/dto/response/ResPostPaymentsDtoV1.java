package com.example.shop.payment.presentation.dto.response;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.payment.domain.entity.PaymentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostPaymentsDtoV1 {

    private PaymentDto payment;

    public static ResPostPaymentsDtoV1 of(PaymentEntity paymentEntity, OrderEntity orderEntity) {
        return ResPostPaymentsDtoV1.builder()
                .payment(PaymentDto.from(paymentEntity, orderEntity))
                .build();
    }

    @Getter
    @Builder
    public static class PaymentDto {

        private String id;
        private PaymentEntity.Status status;
        private PaymentEntity.Method method;
        private Long amount;
        private String transactionKey;
        private String orderId;
        private OrderEntity.Status orderStatus;

        public static PaymentDto from(PaymentEntity paymentEntity, OrderEntity orderEntity) {
            return PaymentDto.builder()
                    .id(paymentEntity.getId().toString())
                    .status(paymentEntity.getStatus())
                    .method(paymentEntity.getMethod())
                    .amount(paymentEntity.getAmount())
                    .transactionKey(paymentEntity.getTransactionKey())
                    .orderId(orderEntity.getId().toString())
                    .orderStatus(orderEntity.getStatus())
                    .build();
        }
    }
}
