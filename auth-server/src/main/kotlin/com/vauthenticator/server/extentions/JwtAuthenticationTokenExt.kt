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
    val aud = this.token.claims["aud"]!!
    return try {
        ClientAppId((aud as String))
    } catch (e: RuntimeException) {
        ClientAppId((aud as List<String>)[0])
    }
}

fun JwtAuthenticationToken.hasEnoughScopes(scopes: Scopes) =
    scopes.content.stream().allMatch { (this.tokenAttributes["scope"] as List<String>).contains(it.content) }
        .or((this.tokenAttributes["scope"] as List<String>).contains(Scope.ADMIN_FULL_ACCESS.content))
        .or(this.authorities.contains(SimpleGrantedAuthority(Role.adminRole().name)))


fun JwtAuthenticationToken.hasEnoughScopes(scope: Scope) = hasEnoughScopes(Scopes(setOf(scope)))