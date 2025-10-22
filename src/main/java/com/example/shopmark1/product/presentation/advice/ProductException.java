package com.example.shopmark1.product.presentation.advice;

import com.example.shopmark1.global.presentation.advice.GlobalError;
import lombok.Getter;

@Getter
public class ProductException extends RuntimeException {

    private final GlobalError error;

    public ProductException(ProductError error) {
        super(error.getErrorMessage());
        this.error = error;
    }
}
