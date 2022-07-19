package com.example.picsingularcore.common.response

import lombok.extern.slf4j.Slf4j
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(exception: Exception): ResponseData<String> {
        return ResponseData.error(code = ResponseCode.INTERNAL_SERVER_ERROR.code,exception.message ?: ResponseCode.INTERNAL_SERVER_ERROR.message)
    }
}