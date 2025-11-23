package com.vauthenticator.server.config

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.login.userdetails.AccountUserDetailsService
import com.vauthenticator.server.login.workflow.CompositeLoginWorkflowEngine
import com.vauthenticator.server.login.workflow.LOGIN_ENGINE_BROKER_PAGE
import com.vauthenticator.server.mfa.web.MfaLoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oidc.logout.ClearSessionStateLogoutHandler
import com.vauthenticator.server.oidc.sessionmanagement.SessionManagementFactory
import com.vauthenticator.server.password.adapter.spring.BcryptVAuthenticatorPasswordEncoder
import com.vauthenticator.server.password.domain.changepassword.CHANGE_PASSWORD_URL
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordLoginWorkflowHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.*
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import java.util.*


const val adminRole = "VAUTHENTICATOR_ADMIN"

const val LOG_IN_URL_PAGE = "/login"
const val FRONT_CHANNEL_LOG_OUT_URL = "/oidc/logout"
const val LOG_OUR_URL = "/logout"
private val WHITE_LIST = arrayOf(
    LOG_IN_URL_PAGE,
    FRONT_CHANNEL_LOG_OUT_URL,
    LOG_OUR_URL
)

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
class WebSecurityConfig(
    private val providerSettings: AuthorizationServerSettings,
    private val redisTemplate: RedisTemplate<String, String?>
) {

    @Bean
    fun defaultSecurityFilterChain(
        http: HttpSecurity,
        loginWorkflowEngine: AuthenticationSuccessHandler,
        accountUserDetailsService: AccountUserDetailsService
    ): SecurityFilterChain {
        http.csrf {
            it.requireCsrfProtectionMatcher(
                OrRequestMatcher(
                    AntPathRequestMatcher("/login", HttpMethod.POST.name()),
                    AntPathRequestMatcher("/mfa-challenge", HttpMethod.POST.name()),
                    AntPathRequestMatcher("/change-password", HttpMethod.POST.name())
                )
            )
        }
        http.headers { it.frameOptions { it.disable() } }

        http.formLogin {
            it.successHandler(loginWorkflowEngine)
                .loginProcessingUrl(LOG_IN_URL_PAGE)
                .loginPage(LOG_IN_URL_PAGE)
                .permitAll()
        }


        http.logout {
            it.addLogoutHandler(
                ClearSessionStateLogoutHandler(
                    SessionManagementFactory(providerSettings),
                    redisTemplate
                )
            )
                .invalidateHttpSession(true)
                .logoutRequestMatcher(
                    AntPathRequestMatcher("/logout"))
        }


        http.userDetailsService(accountUserDetailsService)
        http.oauth2ResourceServer { it.jwt {} }
        http.securityMatcher(*WHITE_LIST, "/api/**", "/mfa-challenge/**", "/change-password", LOGIN_ENGINE_BROKER_PAGE)
            .authorizeHttpRequests { authz ->
                authz
                    .requestMatchers(LOGIN_ENGINE_BROKER_PAGE).permitAll()
                    .requestMatchers("/api/mfa/challenge").authenticated()
                    .requestMatchers(HttpMethod.GET, "/mfa-challenge", "/mfa-challenge/send").authenticated()
                    .requestMatchers(HttpMethod.POST, "/mfa-challenge").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/mfa/enrollment").authenticated()

                    .requestMatchers("/change-password").permitAll()

                    .requestMatchers(HttpMethod.POST, "/api/password")
                    .hasAnyAuthority(Scope.GENERATE_PASSWORD.content)


                    .requestMatchers(HttpMethod.PUT, "/api/reset-password-challenge").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/reset-password/{ticket}").permitAll()

                    .requestMatchers(*WHITE_LIST).permitAll()
                    .requestMatchers("/api/accounts").permitAll()

                    .requestMatchers(HttpMethod.PUT, "/api/sign-up/welcome")
                    .hasAnyAuthority(Scope.WELCOME.content)

                    .requestMatchers(HttpMethod.PUT, "/api/verify-challenge")
                    .hasAnyAuthority(Scope.MAIL_VERIFY.content)

                    .requestMatchers(HttpMethod.PUT, "/api/accounts/password")
                    .hasAnyAuthority(Scope.CHANGE_PASSWORD.content)

                    .requestMatchers(HttpMethod.GET, "/api/email-template")
                    .hasAnyAuthority(Scope.MAIL_TEMPLATE_READER.content)

                    .requestMatchers(HttpMethod.PUT, "/api/email-template")
                    .hasAnyAuthority(Scope.MAIL_TEMPLATE_WRITER.content)

                    .requestMatchers(HttpMethod.GET, "/api/keys")
                    .hasAnyAuthority(Scope.KEY_READER.content)

                    .requestMatchers(HttpMethod.POST, "/api/keys")
                    .hasAnyAuthority(Scope.KEY_EDITOR.content)

                    .requestMatchers(HttpMethod.POST, "/api/keys/rotate")
                    .hasAnyAuthority(Scope.KEY_EDITOR.content)

                    .requestMatchers(HttpMethod.DELETE, "/api/keys")
                    .hasAnyAuthority(Scope.KEY_EDITOR.content)

                    .requestMatchers("/api/**")
                    .authenticated()
            }

        return http.build()
    }

    @Bean
    fun loginWorkflowEngine(
        accountRepository: AccountRepository,
        clientApplicationRepository: ClientApplicationRepository
    ) =
        CompositeLoginWorkflowEngine(
            listOf(
                MfaLoginWorkflowHandler(clientApplicationRepository, "/mfa-challenge/send"),
                ChangePasswordLoginWorkflowHandler(
                    accountRepository,
                    SimpleUrlAuthenticationSuccessHandler(CHANGE_PASSWORD_URL)
                )
            ),
            SimpleUrlAuthenticationSuccessHandler(LOGIN_ENGINE_BROKER_PAGE)
        )


    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val logger: Logger = LoggerFactory.getLogger(JwtAuthenticationConverter::class.java)

        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt: Jwt ->
            val scope = jwt.getClaim<List<String>>("scope")
                .filter { scope -> scope != Scope.OPEN_ID.content }
                .filter { scope -> scope != Scope.EMAIL.content }
                .filter { scope -> scope != Scope.PROFILE.content }
                .map { role: String -> SimpleGrantedAuthority(role) }

            val authoritiesClaims = Optional.ofNullable(jwt.getClaim<List<String>>("authorities"))
                .orElseGet { emptyList() }
                .map { role: String -> SimpleGrantedAuthority(role) }

            logger.debug("authorities: ${authoritiesClaims + scope}")
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
    fun nextHopeLoginWorkflowSuccessHandler(): AuthenticationSuccessHandler {
        return SimpleUrlAuthenticationSuccessHandler(LOGIN_ENGINE_BROKER_PAGE)
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