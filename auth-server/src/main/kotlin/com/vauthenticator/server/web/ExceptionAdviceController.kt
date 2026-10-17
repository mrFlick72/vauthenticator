package com.vauthenticator.server.web

import com.vauthenticator.server.oauth2.clientapp.domain.InsufficientClientApplicationScopeException
import com.vauthenticator.server.role.domain.JwtAuthenticationRequiredException
import com.vauthenticator.server.role.domain.ClientApplicationNotResolvedException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionAdviceController {

    @ExceptionHandler(InsufficientClientApplicationScopeException::class)
    fun insufficientClientApplicationScopeExceptionHandler(ex: InsufficientClientApplicationScopeException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)

    @ExceptionHandler(ClientApplicationNotResolvedException::class)
    fun clientApplicationNotResolvedExceptionHandler(ex: ClientApplicationNotResolvedException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)

    @ExceptionHandler(JwtAuthenticationRequiredException::class)
    fun jwtAuthenticationRequiredExceptionHandler(ex: JwtAuthenticationRequiredException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
            .body(ex.message)
}
