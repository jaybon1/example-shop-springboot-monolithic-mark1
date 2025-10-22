package com.example.shopmark1.payment.presentation.dto.response;

import com.example.shopmark1.order.domain.entity.OrderEntity;
import com.example.shopmark1.order.domain.entity.OrderItemEntity;
import com.example.shopmark1.payment.domain.entity.PaymentEntity;
import com.example.shopmark1.user.domain.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ResGetPaymentsWithIdDtoV1 {

    private Payment payment;

    public static ResGetPaymentsWithIdDtoV1 of(PaymentEntity paymentEntity) {
        return ResGetPaymentsWithIdDtoV1.builder()
                .payment(Payment.from(paymentEntity))
                .build();
    }

    @Getter
    @Builder
    public static class Payment {

        private String id;
        private PaymentEntity.Status status;
        private PaymentEntity.Method method;
        private Long amount;
        private String transactionKey;
        private Instant createdAt;
        private Instant updatedAt;
        private Order order;
        private User user;

        public static Payment from(PaymentEntity paymentEntity) {
            return Payment.builder()
                    .id(paymentEntity.getId().toString())
                    .status(paymentEntity.getStatus())
                    .method(paymentEntity.getMethod())
                    .amount(paymentEntity.getAmount())
                    .transactionKey(paymentEntity.getTransactionKey())
                    .createdAt(paymentEntity.getCreatedAt())
                    .updatedAt(paymentEntity.getUpdatedAt())
                    .order(Order.from(paymentEntity.getOrder()))
                    .user(User.from(paymentEntity.getUser()))
                    .build();
        }

        @Getter
        @Builder
        public static class Order {

            private String id;
            private OrderEntity.Status status;
            private Long totalAmount;
            private Instant createdAt;
            private Instant updatedAt;
            private List<OrderItem> orderItemList;

            public static Order from(OrderEntity orderEntity) {
                return Order.builder()
                        .id(orderEntity.getId().toString())
                        .status(orderEntity.getStatus())
                        .totalAmount(orderEntity.getTotalAmount())
                        .createdAt(orderEntity.getCreatedAt())
                        .updatedAt(orderEntity.getUpdatedAt())
                        .orderItemList(OrderItem.from(orderEntity.getOrderItemList()))
                        .build();
            }
        }

        @Getter
        @Builder
        public static class OrderItem {

            private String id;
            private String productId;
            private String productName;
            private Long unitPrice;
            private Long quantity;
            private Long lineTotal;

            private static List<OrderItem> from(List<OrderItemEntity> orderItemEntityList) {
                return orderItemEntityList.stream()
                        .map(OrderItem::from)
                        .toList();
            }

            public static OrderItem from(OrderItemEntity orderItemEntity) {
                return OrderItem.builder()
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
        public static class User {

            private String id;
            private String username;
            private String nickname;
            private String email;

            public static User from(UserEntity userEntity) {
                return User.builder()
                        .id(userEntity.getId().toString())
                        .username(userEntity.getUsername())
                        .nickname(userEntity.getNickname())
                        .email(userEntity.getEmail())
                        .build();
            }
        }
    }
}
