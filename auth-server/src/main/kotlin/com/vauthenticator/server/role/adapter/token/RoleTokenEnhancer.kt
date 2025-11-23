package com.vauthenticator.server.role.adapter.token

import com.vauthenticator.server.extentions.isATokenForAUserFrom
import org.apache.logging.log4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.util.stream.Collectors

class RoleTokenEnhancer(
    val tokenType: String,
    val roleClaimName: String,
) : OAuth2TokenCustomizer<JwtEncodingContext> {

    private val logger = LoggerFactory.getLogger(RoleTokenEnhancer::class.java)

    override fun customize(context: JwtEncodingContext) {

        logger.info("before teh evaluation")

        if (context.isATokenForAUserFrom()) {
            logger.info("I'm a user token")
            if (tokenType == context.tokenType.value) {
                logger.info("I'm there")
                val attributes = context.authorization!!.attributes
                val principal = attributes["java.security.Principal"] as Authentication

                logger.info("principal.authorities: ${principal.authorities}")
                context.claims.claim(
                    roleClaimName, principal.authorities
                        .stream()
                        .map { obj: GrantedAuthority -> obj.authority }
                        .collect(Collectors.toList()))
            }
        }
    }

}