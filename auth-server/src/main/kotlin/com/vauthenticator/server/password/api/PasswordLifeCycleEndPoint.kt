package com.vauthenticator.server.password.api

import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleExecutor
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordLifeCycleEndPoint(
    private val permissionValidator: PermissionValidator,
    private val passwordLifeCycleExecutor: PasswordLifeCycleExecutor
) {
    @PutMapping("/api/admin/accounts/password/lifecycle")
    fun passwordLifecycle(
        @RequestBody request: PasswordLifeCycleRule,
        principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        passwordLifeCycleExecutor.register(request)
        return ResponseEntity.noContent().build()
    }
}


