package com.vauthenticator.server.role.domain

import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.extentions.hasEnoughScopes
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationNotFound
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.InsufficientClientApplicationScopeException
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

//todo to add scope validation for the admin:full-access scope
class PermissionValidator(private val clientApplicationRepository: ClientApplicationRepository) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(PermissionValidator::class.java.name)
    }

    fun validate(
        principal: JwtAuthenticationToken?,
        session: HttpSession,
        scopes: Scopes
    ) {
        Optional.ofNullable(principal)
            .ifPresentOrElse(
                { principalScopesValidation(it, scopes) },
                { clientAppScopesValidation(session, scopes) }
            )
    }

    //todo to be tested
    fun validate(
        principal: JwtAuthenticationToken,
        scopes: Scopes
    ) {
        principalScopesValidation(principal, scopes)
    }

    private fun clientAppScopesValidation(
        session: HttpSession,
        scopes: Scopes
    ) {
        session.oauth2ClientId()
            .ifPresentOrElse({ clientAppId ->
                clientApplicationRepository.findOne(clientAppId)
                    .ifPresent { clientApplication ->
                        logger.debug("clientApplication.hasEnoughScopes(scopes) ${clientApplication.hasEnoughScopes(scopes)}")
                        logger.debug("scopes ${scopes}")
                        logger.debug("clientApplication.scopes ${clientApplication.scopes}")
                        if (!clientApplication.hasEnoughScopes(scopes)) {
                            throw InsufficientClientApplicationScopeException("The client app ${clientApplication.clientAppId.content} does not support this use case........ consider to add ${scopes.content.map { it.content }} as scope")
                        }
                    }

            }, { throw ClientApplicationNotFound("no client app found") })
    }

    private fun principalScopesValidation(
        principal: JwtAuthenticationToken,
        scopes: Scopes
    ) {
        logger.debug("principal.hasEnoughScopes(scopes) ${principal.hasEnoughScopes(scopes)}")
        logger.debug("scopes ${scopes}")
        logger.debug("principal.scopes ${principal.token.getClaimAsString("scope")}")

        if (!principal.hasEnoughScopes(scopes)) {
            throw InsufficientClientApplicationScopeException("The client app ${principal.clientAppId().content} used by a principal does not support this use case........ consider to add ${scopes.content.map { it.content }} as scope")
        }
    }
}