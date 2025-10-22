package com.example.shopmark1.order.presentation.advice;

import com.example.shopmark1.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

    private final GlobalError error;

    public OrderException(OrderError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}

