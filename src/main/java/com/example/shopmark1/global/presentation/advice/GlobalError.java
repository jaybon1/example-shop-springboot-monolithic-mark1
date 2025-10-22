package com.example.shopmark1.global.presentation.advice;

import org.springframework.http.HttpStatus;

public interface GlobalError {

    HttpStatus getHttpStatus();
    String getErrorCode();
    String getErrorMessage();

}
