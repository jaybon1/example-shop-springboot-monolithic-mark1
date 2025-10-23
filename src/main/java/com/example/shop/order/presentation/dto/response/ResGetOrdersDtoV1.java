package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.entity.OrderEntity.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ResGetOrdersDtoV1 {

    private OrderPage orderPage;

    @Getter
    @ToString
    public static class OrderPage extends PagedModel<OrderPage.Order> {

        public OrderPage(Page<OrderEntity> orderEntityPage) {
            super(
                    new PageImpl<>(
                            Order.from(orderEntityPage.getContent()),
                            orderEntityPage.getPageable(),
                            orderEntityPage.getTotalElements()
                    )
            );
        }

        @Getter
        @Builder
        public static class Order {

            private String id;
            private Status status;
            private Long totalAmount;
            private Instant createdAt;
            private Instant updatedAt;

            private static List<Order> from(List<OrderEntity> orderEntityList) {
                return orderEntityList.stream()
                        .map(Order::from)
                        .toList();
            }

            public static Order from(OrderEntity orderEntity) {
                return Order.builder()
                        .id(orderEntity.getId().toString())
                        .status(orderEntity.getStatus())
                        .totalAmount(orderEntity.getTotalAmount())
                        .createdAt(orderEntity.getCreatedAt())
                        .updatedAt(orderEntity.getUpdatedAt())
                        .build();
            }
        }
    }
}
