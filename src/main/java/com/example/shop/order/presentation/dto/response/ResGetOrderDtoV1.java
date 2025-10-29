package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.entity.OrderEntity.Status;
import com.example.shop.order.domain.entity.OrderItemEntity;
import com.example.shop.payment.domain.entity.PaymentEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ResGetOrderDtoV1 {

    private OrderDto order;

    public static ResGetOrderDtoV1 of(OrderEntity orderEntity) {
        return ResGetOrderDtoV1.builder()
                .order(OrderDto.from(orderEntity))
                .build();
    }

    @Getter
    @Builder
    public static class OrderDto {

        private String id;
        private Status status;
        private Long totalAmount;
        private Instant createdAt;
        private Instant updatedAt;
        private List<OrderItemDto> orderItemList;
        private PaymentDto payment;

        public static OrderDto from(OrderEntity orderEntity) {
            return OrderDto.builder()
                    .id(orderEntity.getId().toString())
                    .status(orderEntity.getStatus())
                    .totalAmount(orderEntity.getTotalAmount())
                    .createdAt(orderEntity.getCreatedAt())
                    .updatedAt(orderEntity.getUpdatedAt())
                    .orderItemList(OrderItemDto.from(orderEntity.getOrderItemList()))
                    .payment(PaymentDto.from(orderEntity.getPayment()))
                    .build();
        }

        @Getter
        @Builder
        public static class OrderItemDto {

            private String id;
            private String productId;
            private String productName;
            private Long unitPrice;
            private Long quantity;
            private Long lineTotal;

            private static List<OrderItemDto> from(List<OrderItemEntity> orderItemEntityList) {
                return orderItemEntityList.stream()
                        .map(OrderItemDto::from)
                        .toList();
            }

            public static OrderItemDto from(OrderItemEntity orderItemEntity) {
                return OrderItemDto.builder()
                        .id(orderItemEntity.getId().toString())
                        .productId(orderItemEntity.getProductId().toString())
                        .productName(orderItemEntity.getProductName())
                        .unitPrice(orderItemEntity.getUnitPrice())
                        .quantity(orderItemEntity.getQuantity())
                        .lineTotal(orderItemEntity.getLineTotal())
                        .build();
            }
        }

        @Getter
        @Builder
        public static class PaymentDto {

            private String id;
            private PaymentEntity.Status status;
            private PaymentEntity.Method method;
            private Long amount;
            private String transactionKey;

            public static PaymentDto from(PaymentEntity paymentEntity) {
                if (paymentEntity == null) {
                    return null;
                }
                return PaymentDto.builder()
                        .id(paymentEntity.getId().toString())
                        .status(paymentEntity.getStatus())
                        .method(paymentEntity.getMethod())
                        .amount(paymentEntity.getAmount())
                        .transactionKey(paymentEntity.getTransactionKey())
                        .build();
            }
        }
    }
}
