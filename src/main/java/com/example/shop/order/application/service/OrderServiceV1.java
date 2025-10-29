package com.example.shop.order.application.service;

import com.example.shop.order.domain.entity.OrderEntity;
import com.example.shop.order.domain.entity.OrderEntity.Status;
import com.example.shop.order.domain.entity.OrderItemEntity;
import com.example.shop.order.domain.repository.OrderRepository;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import com.example.shop.payment.domain.entity.PaymentEntity;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.product.domain.entity.ProductEntity;
import com.example.shop.product.domain.repository.ProductRepository;
import com.example.shop.user.domain.entity.UserEntity;
import com.example.shop.user.domain.entity.UserRoleEntity;
import com.example.shop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ResGetOrdersDtoV1 getOrders(UUID authUserId, List<String> authUserRoleList, Pageable pageable) {
        Page<OrderEntity> orderEntityPage;
        if (isAdminOrManager(authUserRoleList)) {
            orderEntityPage = orderRepository.findAll(pageable);
        } else {
            orderEntityPage = orderRepository.findByUser_Id(authUserId, pageable);
        }
        return ResGetOrdersDtoV1.builder()
                .orderPage(new ResGetOrdersDtoV1.OrderPage(orderEntityPage))
                .build();
    }

    public ResGetOrderDtoV1 getOrder(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        OrderEntity orderEntity = getOrderForUser(orderId, authUserId, authUserRoleList);
        return ResGetOrderDtoV1.of(orderEntity);
    }

    @Transactional
    public ResPostOrdersDtoV1 postOrders(UUID authUserId, ReqPostOrdersDtoV1 reqDto) {

        List<ReqPostOrdersDtoV1.Order.OrderItem> reqOrderItemList = reqDto.getOrder().getOrderItemList();

        Set<UUID> productIdSet = reqOrderItemList.stream()
                .map(ReqPostOrdersDtoV1.Order.OrderItem::getProductId)
                .collect(Collectors.toSet());

        Map<UUID, ProductEntity> productMap = productRepository.findAllById(productIdSet)
                .stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        if (productMap.size() != productIdSet.size()) {
            throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
        }

        UserEntity orderUserEntity = userRepository.findDefaultById(authUserId);

        OrderEntity orderEntity = OrderEntity.builder()
                .user(orderUserEntity)
                .status(Status.CREATED)
                .build();

        long totalAmount = 0L;
        for (ReqPostOrdersDtoV1.Order.OrderItem reqOrderItem : reqOrderItemList) {
            UUID productId = reqOrderItem.getProductId();
            Long quantityValue = reqOrderItem.getQuantity();

            ProductEntity productEntity = productMap.get(productId);

            long quantity = quantityValue;
            long currentStock = productEntity.getStock();
            long updatedStock = currentStock - quantity;
            if (updatedStock < 0) {
                throw new OrderException(OrderError.ORDER_PRODUCT_OUT_OF_STOCK);
            }

            productEntity.update(null, null, updatedStock);

            long lineTotal = safeMultiply(productEntity.getPrice(), quantity);
            totalAmount = safeAdd(totalAmount, lineTotal);

            OrderItemEntity orderItemEntity = OrderItemEntity.builder()
                    .productId(productEntity.getId())
                    .productName(productEntity.getName())
                    .unitPrice(productEntity.getPrice())
                    .quantity(quantityValue)
                    .lineTotal(lineTotal)
                    .build();
            orderEntity.addOrderItem(orderItemEntity);
        }

        orderEntity.updateTotalAmount(totalAmount);
        OrderEntity savedOrderEntity = orderRepository.save(orderEntity);

        return ResPostOrdersDtoV1.of(savedOrderEntity, null);
    }

    @Transactional
    public void postOrderCancel(UUID authUserId, List<String> authUserRoleList, UUID orderId) {
        OrderEntity orderEntity = getOrderForUser(orderId, authUserId, authUserRoleList);

        if (Status.CANCELLED.equals(orderEntity.getStatus())) {
            throw new OrderException(OrderError.ORDER_ALREADY_CANCELLED);
        }

        PaymentEntity paymentEntity = orderEntity.getPayment();
        if (Status.PAID.equals(orderEntity.getStatus())) {
            if (paymentEntity == null) {
                throw new PaymentException(PaymentError.PAYMENT_NOT_FOUND);
            }
            cancelPaymentForOrder(paymentEntity, authUserId, authUserRoleList);
        } else if (paymentEntity != null) {
            cancelPaymentForOrder(paymentEntity, authUserId, authUserRoleList);
        }

        restoreProductStock(orderEntity);
        orderEntity.markCancelled();
    }

    private void cancelPaymentForOrder(PaymentEntity paymentEntity, UUID authUserId, List<String> authUserRoleList) {
        if (PaymentEntity.Status.CANCELLED.equals(paymentEntity.getStatus())) {
            throw new PaymentException(PaymentError.PAYMENT_ALREADY_CANCELLED);
        }

        if (!paymentEntity.getUser().getId().equals(authUserId) && !isAdminOrManager(authUserRoleList)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }

        paymentEntity.markCancelled();
    }

    private void restoreProductStock(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemList = orderEntity.getOrderItemList();
        if (orderItemList.isEmpty()) {
            return;
        }

        Set<UUID> productIds = orderItemList.stream()
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        Map<UUID, ProductEntity> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        for (OrderItemEntity orderItemEntity : orderItemList) {
            ProductEntity productEntity = productMap.get(orderItemEntity.getProductId());
            if (productEntity == null) {
                throw new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND);
            }

            long restoredStock = safeAdd(productEntity.getStock(), orderItemEntity.getQuantity());
            productEntity.update(null, null, restoredStock);
        }
    }

    private OrderEntity getOrderForUser(UUID orderId, UUID authUserId, List<String> authUserRoleList) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderError.ORDER_NOT_FOUND));

        if (isAdminOrManager(authUserRoleList)) {
            return orderEntity;
        }

        if (orderEntity.getUser().getId().equals(authUserId)) {
            return orderEntity;
        }

        throw new OrderException(OrderError.ORDER_FORBIDDEN);
    }

    private boolean isAdminOrManager(List<String> authUserRoleList) {
        if (authUserRoleList == null) {
            return false;
        }
        return authUserRoleList.contains(UserRoleEntity.Role.ADMIN.toString())
                || authUserRoleList.contains(UserRoleEntity.Role.MANAGER.toString());
    }

    private long safeMultiply(Long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException e) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }

    private long safeAdd(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException e) {
            throw new OrderException(OrderError.ORDER_AMOUNT_OVERFLOW);
        }
    }

}
