package it.valeriovaudi.vauthenticator.support

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = OAuth2WithSecurityContextFactory::class)
annotation class WithMockOAuth2User(val username: String = "A_USER_NAME", val userNameKey: String = "user_name",
                                    val authoritiesKey: String = "authorities", val authorities: String = "USER")

internal class OAuth2WithSecurityContextFactory : WithSecurityContextFactory<WithMockOAuth2User> {

    override fun createSecurityContext(annotation: WithMockOAuth2User): SecurityContext {
        val securityContext = SecurityContextHolder.createEmptyContext();

        val jwt = Jwt("A_TOKEN", null, null,
                mapOf("HEADER" to "VALUE"), mapOf(annotation.userNameKey to annotation.username, annotation.authoritiesKey to annotation.authorities.split(",")))

        securityContext.authentication = JwtAuthenticationToken(jwt)
        securityContext.authentication.isAuthenticated = true

        return securityContext
    }

}