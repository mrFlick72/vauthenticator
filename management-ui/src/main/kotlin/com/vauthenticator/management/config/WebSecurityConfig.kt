package com.vauthenticator.management.config

import com.vauthenticator.springbootclientstarter.filter.BearerTokenInterceptor
import com.vauthenticator.springbootclientstarter.filter.OAuth2TokenResolver
import com.vauthenticator.springbootclientstarter.session.management.OAuth2AuthorizationRequestResolverWithSessionState
import com.vauthenticator.springbootclientstarter.user.VAuthenticatorOidcUserService
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain


const val adminRole = "VAUTHENTICATOR_ADMIN"

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class WebSecurityConfig(
    private val vAuthenticatorOidcUserService: VAuthenticatorOidcUserService,
    private val oAuth2AuthorizationRequestResolverWithSessionState: OAuth2AuthorizationRequestResolverWithSessionState
) {

    @Bean
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf().disable().headers().frameOptions().disable()

        http.logout()
            .deleteCookies("opbs")
            .invalidateHttpSession(true)
            .logoutSuccessUrl("/secure/admin/index")


        http.oauth2Login().defaultSuccessUrl("/secure/admin/index")
            .userInfoEndpoint()
            .oidcUserService(vAuthenticatorOidcUserService)
            .and()
            .authorizationEndpoint()
            .authorizationRequestResolver(oAuth2AuthorizationRequestResolverWithSessionState);

        http.authorizeHttpRequests { authz ->
            authz.requestMatchers("/static/content/asset/**","/actuator/**", "/oidc_logout.html").permitAll()
                .anyRequest().hasAnyAuthority(adminRole)
        }

        return http.build()
    }

    @Bean
    fun vauthenticatorRestTemplate(oAuth2TokenResolver: OAuth2TokenResolver) =
        RestTemplateBuilder()
            .additionalInterceptors(BearerTokenInterceptor(oAuth2TokenResolver))
            .requestFactory(HttpComponentsClientHttpRequestFactory::class.java)
            .build()

}