package com.example.shopmark1.order.domain.entity;

import com.example.shopmark1.global.entity.BaseEntity;
import com.example.shopmark1.payment.domain.entity.PaymentEntity;
import com.example.shopmark1.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "`ORDER`")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder.Default
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItemList = new ArrayList<>();

    @OneToOne(mappedBy = "order")
    private PaymentEntity payment;

    public void addOrderItem(OrderItemEntity orderItemEntity) {
        orderItemEntity.setOrder(this);
        this.orderItemList.add(orderItemEntity);
    }

    public void markPaid() {
        this.status = Status.PAID;
    }

    public void markCancelled() {
        this.status = Status.CANCELLED;
    }

    public void updateTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void assignPayment(PaymentEntity paymentEntity) {
        this.payment = paymentEntity;
    }

    public void assignUser(UserEntity userEntity) {
        this.user = userEntity;
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELLED
    }
}
