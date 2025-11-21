package com.vauthenticator.server.web

import com.vauthenticator.server.oauth2.clientapp.domain.InsufficientClientApplicationScopeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionAdviceController {

    @ExceptionHandler(InsufficientClientApplicationScopeException::class)
    fun insufficientClientApplicationScopeExceptionHandler(ex: InsufficientClientApplicationScopeException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)
}