package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.login.failedloginlock.adapter.AccountRepositoryFailedLoginLockRepository
import com.vauthenticator.server.login.failedloginlock.adapter.jdbc.JdbcFailedLoginLockRepository
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

    @Bean("failedLoginLockRepository")
    @Profile("database")
    fun jdbcFailedLoginLockRepository(jdbcTemplate: JdbcTemplate): FailedLoginLockRepository =
        JdbcFailedLoginLockRepository(jdbcTemplate)

    @Bean("failedLoginLockRepository")
    @Profile("dynamo")
    fun accountRepositoryFailedLoginLockRepository(accountRepository: AccountRepository): FailedLoginLockRepository =
        AccountRepositoryFailedLoginLockRepository(accountRepository)

    @Bean
    fun failedLoginLock(
        accountRepository: AccountRepository,
        failedLoginLockRepository: FailedLoginLockRepository,
        consecutiveFailedLogins: ConsecutiveFailedLogins,
        eventsDispatcher: VAuthenticatorEventsDispatcher,
        clock: Clock,
        failedLoginLockConfig: FailedLoginLockConfig
    ) = FailedLoginLock(
        accountRepository,
        failedLoginLockRepository,
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
