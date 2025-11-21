package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.welcome.SayWelcome
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WelcomeMailEndPoint(
    private val permissionValidator: PermissionValidator,
    private val sayWelcome: SayWelcome
) {

    @PutMapping("/api/sign-up/welcome")
    fun welcome(
        @RequestBody request: Map<String, String>,
        session: HttpSession, principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, session, Scopes.from(Scope.WELCOME))
        sayWelcome.welcome(request["email"]!!)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun noAccountExceptionHAndler() =
        ResponseEntity.notFound().build<Unit>()
}