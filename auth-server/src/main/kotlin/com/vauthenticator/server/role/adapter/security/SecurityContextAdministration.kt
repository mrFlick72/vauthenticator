package com.vauthenticator.server.role.adapter.security

import com.vauthenticator.server.extentions.isAdministrator
import com.vauthenticator.server.role.domain.Administration
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * The caller is the one in the Spring Security context. Only an access token can be an Administrator:
 * a browser session never is, even when its account has the Administrator Role.
 */
class SecurityContextAdministration : Administration {

    override fun requireAdministrator() {
        val caller = SecurityContextHolder.getContext().authentication as? JwtAuthenticationToken
        if (caller?.isAdministrator() != true) {
            throw AdministrationRequiredException("only an administrator can change administration")
        }
    }
}
