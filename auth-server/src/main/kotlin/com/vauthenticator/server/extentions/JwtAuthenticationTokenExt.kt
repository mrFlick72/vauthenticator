package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.Role
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun Authentication.clientAppId(): ClientAppId {
    val authentication = this as JwtAuthenticationToken
    return authentication.clientAppId()
}

fun JwtAuthenticationToken.clientAppId(): ClientAppId {
    val clientAppId = this.token.claims["aud"]
    ?: throw RuntimeException("JWT token has no aud claim") //todo it should be a custom exception
    return try {
        ClientAppId((clientAppId as String))
    } catch (e: RuntimeException) {
        ClientAppId((clientAppId as List<String>)[0])
    }
}

fun JwtAuthenticationToken.hasEnoughScopes(scopes: Scopes) =
    scopes.content.stream().allMatch { (this.tokenAttributes["scope"] as List<String>).contains(it.content) }
        .or((this.tokenAttributes["scope"] as List<String>).contains(Scope.ADMIN_FULL_ACCESS.content))
        .or(this.authorities.contains(SimpleGrantedAuthority(Role.adminRole().name)))


fun JwtAuthenticationToken.hasEnoughScopes(scope: Scope) = hasEnoughScopes(Scopes(setOf(scope)))