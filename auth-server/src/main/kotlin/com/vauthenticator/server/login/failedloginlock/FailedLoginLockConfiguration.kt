package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.login.failedloginlock.adapter.AccountRepositoryFailedLoginLockStore
import com.vauthenticator.server.login.failedloginlock.adapter.jdbc.JdbcFailedLoginLockStore
import com.vauthenticator.server.login.failedloginlock.adapter.redis.RedisConsecutiveFailedLogins
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Clock

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FailedLoginLockConfig::class)
class FailedLoginLockConfiguration {

    @Bean
    fun consecutiveFailedLogins(redisTemplate: StringRedisTemplate): ConsecutiveFailedLogins =
        RedisConsecutiveFailedLogins(redisTemplate)

    @Bean("failedLoginLockStore")
    @Profile("database")
    fun jdbcFailedLoginLockStore(jdbcTemplate: JdbcTemplate): FailedLoginLockStore =
        JdbcFailedLoginLockStore(jdbcTemplate)

    @Bean("failedLoginLockStore")
    @Profile("dynamo")
    fun accountRepositoryFailedLoginLockStore(accountRepository: AccountRepository): FailedLoginLockStore =
        AccountRepositoryFailedLoginLockStore(accountRepository)

    @Bean
    fun failedLoginLock(
        accountRepository: AccountRepository,
        failedLoginLockStore: FailedLoginLockStore,
        consecutiveFailedLogins: ConsecutiveFailedLogins,
        eventsDispatcher: VAuthenticatorEventsDispatcher,
        clock: Clock,
        failedLoginLockConfig: FailedLoginLockConfig
    ) = FailedLoginLock(
        accountRepository,
        failedLoginLockStore,
        consecutiveFailedLogins,
        eventsDispatcher,
        clock,
        failedLoginLockConfig
    )

    @Bean
    fun failedLoginLockAuthenticationListener(
        failedLoginLock: FailedLoginLock,
        currentHttpServletRequestService: CurrentHttpServletRequestService
    ) = FailedLoginLockAuthenticationListener(failedLoginLock, currentHttpServletRequestService)
}
