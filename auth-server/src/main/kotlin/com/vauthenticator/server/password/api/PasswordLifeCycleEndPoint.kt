package com.vauthenticator.server.password.api

import com.vauthenticator.server.password.domain.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.PasswordLifeCycleStrategyExecutor
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordLifeCycleEndPoint(
    private val passwordLifeCycleStrategyExecutor: PasswordLifeCycleStrategyExecutor
) {

    @PutMapping("/api/admin/accounts/password/lifecycle")
    fun passwordLifecycle(
        @RequestBody request: PasswordLifeCycleRule,
        principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        passwordLifeCycleStrategyExecutor.execute(request)
        return ResponseEntity.noContent().build()
    }
}


