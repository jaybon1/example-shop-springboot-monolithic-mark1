package com.example.shop.payment.presentation.dto.request;

import com.example.shop.payment.domain.entity.PaymentEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReqPostPaymentsDtoV1 {

    @NotNull(message = "결제 정보를 입력해주세요.")
    @Valid
    private Payment payment;

    @Getter
    @Builder
    public static class Payment {

        @NotNull(message = "주문 ID를 입력해주세요.")
        private UUID orderId;

        @NotNull(message = "결제 수단을 입력해주세요.")
        private PaymentEntity.Method method;

        private String transactionKey;
    }
}
