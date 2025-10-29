package com.example.shop.payment.presentation.dto.response;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.entity.OrderItemEntity;
import com.example.shop.payment.domain.entity.PaymentEntity;
import com.example.shop.user.domain.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ResGetPaymentDtoV1 {

    private PaymentDto payment;

    public static ResGetPaymentDtoV1 of(PaymentEntity paymentEntity) {
        return ResGetPaymentDtoV1.builder()
                .payment(PaymentDto.from(paymentEntity))
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
        private Instant createdAt;
        private Instant updatedAt;
        private OrderDto order;
        private UserDto user;

        public static PaymentDto from(PaymentEntity paymentEntity) {
            return PaymentDto.builder()
                    .id(paymentEntity.getId().toString())
                    .status(paymentEntity.getStatus())
                    .method(paymentEntity.getMethod())
                    .amount(paymentEntity.getAmount())
                    .transactionKey(paymentEntity.getTransactionKey())
                    .createdAt(paymentEntity.getCreatedAt())
                    .updatedAt(paymentEntity.getUpdatedAt())
                    .order(OrderDto.from(paymentEntity.getOrder()))
                    .user(UserDto.from(paymentEntity.getUser()))
                    .build();
        }

        @Getter
        @Builder
        public static class OrderDto {

            private String id;
            private OrderEntity.Status status;
            private Long totalAmount;
            private Instant createdAt;
            private Instant updatedAt;
            private List<OrderItemDto> orderItemList;

            public static OrderDto from(OrderEntity orderEntity) {
                return OrderDto.builder()
                        .id(orderEntity.getId().toString())
                        .status(orderEntity.getStatus())
                        .totalAmount(orderEntity.getTotalAmount())
                        .createdAt(orderEntity.getCreatedAt())
                        .updatedAt(orderEntity.getUpdatedAt())
                        .orderItemList(OrderItemDto.from(orderEntity.getOrderItemList()))
                        .build();
            }
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
        public static class UserDto {

            private String id;
            private String username;
            private String nickname;
            private String email;

            public static UserDto from(UserEntity userEntity) {
                return UserDto.builder()
                        .id(userEntity.getId().toString())
                        .username(userEntity.getUsername())
                        .nickname(userEntity.getNickname())
                        .email(userEntity.getEmail())
                        .build();
            }
        }
    }
}
