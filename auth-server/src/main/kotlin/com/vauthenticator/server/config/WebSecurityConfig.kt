package com.vauthenticator.server.config

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.login.userdetails.AccountUserDetailsService
import com.vauthenticator.server.login.workflow.LoginWorkflowEngine
import com.vauthenticator.server.mfa.domain.MfaLoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.password.adapter.spring.BcryptVAuthenticatorPasswordEncoder
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordLoginWorkflowHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.RedirectStrategy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.authentication.*
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.web.cors.CorsConfigurationSource


const val adminRole = "VAUTHENTICATOR_ADMIN"

const val LOG_IN_URL_PAGE = "/login"
private val WHITE_LIST = arrayOf(
    LOG_IN_URL_PAGE,
)

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class WebSecurityConfig {
    @Autowired
    lateinit var corsConfigurationSource: CorsConfigurationSource

    @Bean
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        loginWorkflowEngine: AuthenticationSuccessHandler,
        accountUserDetailsService: AccountUserDetailsService
    ): SecurityFilterChain {
        http.csrf {
            it.requireCsrfProtectionMatcher(
                OrRequestMatcher(
                    PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login"),
                    PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/mfa-challenge"),
                    PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/change-password")
                )
            )
        }
        http.cors { it.configurationSource(corsConfigurationSource) }
        http.headers { it.frameOptions { it.disable() } }

        http.formLogin {
            it.successHandler(loginWorkflowEngine)
                .loginProcessingUrl(LOG_IN_URL_PAGE)
                .loginPage(LOG_IN_URL_PAGE)
                .permitAll()
        }


        http.userDetailsService(accountUserDetailsService)
        http.oauth2ResourceServer { it.jwt {} }
        http.securityMatcher(*WHITE_LIST, "/api/**", "/mfa-challenge/**", "/change-password")
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(HttpMethod.GET, "/mfa-challenge", "/mfa-challenge/send").authenticated()
                    .requestMatchers(HttpMethod.POST, "/mfa-challenge").authenticated()

                    .requestMatchers("/change-password").permitAll()

                    .requestMatchers(HttpMethod.PUT, "/api/reset-password-challenge").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/reset-password/{ticket}").permitAll()

                    .requestMatchers(*WHITE_LIST).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accounts").permitAll()

                    .requestMatchers("/api/**")
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun mfaLoginWorkflowHandler(clientApplicationRepository: ClientApplicationRepository) =
        MfaLoginWorkflowHandler(clientApplicationRepository, "/mfa-challenge/send")

    @Bean
    fun changePasswordLoginWorkflowHandler(accountRepository: AccountRepository) =
        ChangePasswordLoginWorkflowHandler(accountRepository)

    @Bean
    fun authenticationTrustResolver(): AuthenticationTrustResolver = AuthenticationTrustResolverImpl()

    @Bean
    fun loginWorkflowRequestCache(): RequestCache = HttpSessionRequestCache()

    @Bean
    fun loginWorkflowRedirectStrategy(): RedirectStrategy = DefaultRedirectStrategy()

    @Bean
    fun loginWorkflowEngine(
        mfaLoginWorkflowHandler: MfaLoginWorkflowHandler,
        changePasswordLoginWorkflowHandler: ChangePasswordLoginWorkflowHandler,
        loginWorkflowRequestCache: RequestCache,
        loginWorkflowRedirectStrategy: RedirectStrategy,
        successHandler: AuthenticationSuccessHandler
    ) =
        LoginWorkflowEngine(
            listOf(mfaLoginWorkflowHandler, changePasswordLoginWorkflowHandler),
            loginWorkflowRequestCache,
            loginWorkflowRedirectStrategy,
            successHandler
        )



    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
            val scope = jwt.getClaim<List<String>>("scope")
                ?.filter { scope -> scope != Scope.OPEN_ID.content }
                ?.filter { scope -> scope != Scope.EMAIL.content }
                ?.filter { scope -> scope != Scope.PROFILE.content }
                ?.map { role: String -> SimpleGrantedAuthority(role) }
                .orEmpty()

            val authoritiesClaims = (jwt.getClaim<List<String>>("authorities") ?: emptyList())
                .map { role: String -> SimpleGrantedAuthority(role) }

            logger.debug("authorities: {}", authoritiesClaims + scope)
            authoritiesClaims + scope
        }
        jwtAuthenticationConverter.setPrincipalClaimName("user_name")
        return jwtAuthenticationConverter
    }

    @Bean
    fun bcryptAccountPasswordEncoder(passwordEncoder: PasswordEncoder) =
        BcryptVAuthenticatorPasswordEncoder(passwordEncoder)

    @Bean("passwordEncoder")
    @ConditionalOnProperty(prefix = "password-encoder.implementation", name = ["bcrypt"], matchIfMissing = true)
    fun bcryptPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)
    }

    @Bean("passwordEncoder")
    @ConditionalOnProperty(prefix = "password-encoder.implementation", name = ["argon2"], matchIfMissing = false)
    fun argon2PasswordEncoder(): PasswordEncoder {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
    }

    @Bean
    fun accountUserDetailsService(userRepository: AccountRepository) =
        AccountUserDetailsService(userRepository)

    @Bean
    fun successHandler(): AuthenticationSuccessHandler {
        return SavedRequestAwareAuthenticationSuccessHandler()
    }

    @Bean
    fun mfaFailureHandler(): AuthenticationFailureHandler {
        return SimpleUrlAuthenticationFailureHandler("/mfa-challenge?error")
    }

    @Bean
    fun changePasswordFailureHandler(): AuthenticationFailureHandler {
        return SimpleUrlAuthenticationFailureHandler("/change-password?error")
    }

    @Bean
    fun failureHandler(): AuthenticationFailureHandler {
        return SimpleUrlAuthenticationFailureHandler("/login?error")
    }

}
