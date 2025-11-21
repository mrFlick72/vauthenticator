package com.vauthenticator.server.config

import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.keys.adapter.spring.KeysJWKSource
import com.vauthenticator.server.keys.domain.KeyDecrypter
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.lambdas.AwsLambdaFunctionContextFactory
import com.vauthenticator.server.lambdas.LambdaFunction
import com.vauthenticator.server.lambdas.LambdaTokenEnhancer
import com.vauthenticator.server.oauth2.authorizationservice.RedisOAuth2AuthorizationService
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.AVAILABLE_SCOPES
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.OPEN_ID
import com.vauthenticator.server.oauth2.clientapp.domain.StoreClientApplication
import com.vauthenticator.server.oauth2.registeredclient.ClientAppRegisteredClientRepository
import com.vauthenticator.server.oauth2.token.OAuth2TokenEnhancer
import com.vauthenticator.server.oidc.sessionmanagement.SessionManagementFactory
import com.vauthenticator.server.oidc.sessionmanagement.sendAuthorizationResponse
import com.vauthenticator.server.oidc.token.IdTokenEnhancer
import com.vauthenticator.server.oidc.userinfo.UserInfoEnhancer
import com.vauthenticator.server.role.adapter.token.GroupTokenEnhancer
import com.vauthenticator.server.role.adapter.token.RoleTokenEnhancer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfigurationSource


@Configuration(proxyBeanMethods = false)
class AuthorizationServerConfig {

    @Value("\${auth.oidcIss:}")
    lateinit var oidcIss: String

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var corsConfigurationSource: CorsConfigurationSource

    @Bean
    fun jwkSource(keyRepository: KeyRepository, keyDecrypter: KeyDecrypter): JWKSource<SecurityContext?> =
        KeysJWKSource(keyDecrypter, keyRepository)

    @Bean
    fun nimbusJwsEncoder(jwkSource: JWKSource<SecurityContext?>?): JwtEncoder {
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext?>?): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }

    @Bean
    fun jwtCustomizer(
        keyRepository: KeyRepository,
        lambdaFunction: LambdaFunction,
        clientApplicationRepository: ClientApplicationRepository,
        accountRepository: AccountRepository,
        @Value("\${vauthenticator.lambda.aws.enabled:false}") enabled: Boolean,
        @Value("\${vauthenticator.lambda.aws.function-name:vauthenticator-token-enhancer}") lambdaName: String,

        ): OAuth2TokenCustomizer<JwtEncodingContext> {
        return OAuth2TokenCustomizer { context: JwtEncodingContext ->
            val assignedKeys = mutableSetOf<Kid>()
            RoleTokenEnhancer("id_token", "authorities").customize(context)
            RoleTokenEnhancer(OAuth2TokenType.ACCESS_TOKEN.value, "authorities").customize(context)

            GroupTokenEnhancer("id_token", "authorities", accountRepository).customize(context)
            GroupTokenEnhancer(OAuth2TokenType.ACCESS_TOKEN.value, "authorities", accountRepository).customize(context)

            OAuth2TokenEnhancer(assignedKeys, keyRepository, clientApplicationRepository).customize(context)
            IdTokenEnhancer(assignedKeys, keyRepository).customize(context)
            LambdaTokenEnhancer(enabled, lambdaName, lambdaFunction, AwsLambdaFunctionContextFactory(accountRepository))
                .customize(context)
        }
    }

    @Bean
    fun registeredClientRepository(
        storeClientApplication: StoreClientApplication,
        clientRepository: ClientApplicationRepository
    ): RegisteredClientRepository {
        return ClientAppRegisteredClientRepository(storeClientApplication, clientRepository)
    }

    @Bean("oAuth2AuthorizationService")
    @Profile("!database")
    fun redisOAuth2AuthorizationService(redisTemplate: RedisTemplate<Any, Any>): OAuth2AuthorizationService {
        return RedisOAuth2AuthorizationService(redisTemplate)
    }


    @Bean("oAuth2AuthorizationService")
    @Profile("database")
    fun jdbcOAuth2AuthorizationService(
        jdbcTemplate: JdbcTemplate,
        registeredClientRepository: RegisteredClientRepository
    ): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository)
    }

    @Bean
    fun providerSettings(): AuthorizationServerSettings =
        AuthorizationServerSettings.builder().issuer(oidcIss).build()


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun authorizationServerSecurityFilterChain(
        providerSettings: AuthorizationServerSettings,
        redisTemplate: RedisTemplate<String, String?>,
        http: HttpSecurity
    ): SecurityFilterChain {

        val userInfoEnhancer = UserInfoEnhancer(accountRepository)

        val authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer()
        http
            .securityMatcher(authorizationServerConfigurer.endpointsMatcher)
            .with(
                authorizationServerConfigurer, { it.oidc(Customizer.withDefaults()) }// Enable OpenID Connect 1.0
            )
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            // Redirect to the login page when not authenticated from the
            // authorization endpoint
            .exceptionHandling {
                it.defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            }
        http.csrf { it.disable() }.headers { it.frameOptions { it.disable() } }
        http.cors { it.configurationSource(corsConfigurationSource) }

        authorizationServerConfigurer.oidc { configurer ->
            configurer.userInfoEndpoint { customizer ->
                customizer.userInfoMapper { context ->
                    userInfoEnhancer.oidcUserInfoFrom(context)
                }
            }.providerConfigurationEndpoint { customizer ->
                customizer.providerConfigurationCustomizer { providerConfiguration ->
                    AVAILABLE_SCOPES
                        .filter { it != OPEN_ID }
                        .forEach { providerConfiguration.scope(it.content) }
                }
            }.logoutEndpoint {}
        }
            .authorizationEndpoint {
                it.authorizationResponseHandler(
                    sendAuthorizationResponse(
                        redisTemplate,
                        SessionManagementFactory(providerSettings),
                        DefaultRedirectStrategy()
                    )
                )
            }
        http.exceptionHandling { it.authenticationEntryPoint(LoginUrlAuthenticationEntryPoint("/login")) }
            .oauth2ResourceServer { it.jwt {} }

        return http.build()
    }

}

