package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.login.failedloginlock.adapter.redis.RedisConsecutiveFailedLogins
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Clock

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FailedLoginLockConfig::class)
class FailedLoginLockConfiguration {

    @Bean
    fun consecutiveFailedLogins(redisTemplate: StringRedisTemplate): ConsecutiveFailedLogins =
        RedisConsecutiveFailedLogins(redisTemplate)

    @Bean
    fun failedLoginLock(
        accountRepository: AccountRepository,
        consecutiveFailedLogins: ConsecutiveFailedLogins,
        eventsDispatcher: VAuthenticatorEventsDispatcher,
        clock: Clock,
        failedLoginLockConfig: FailedLoginLockConfig
    ) = FailedLoginLock(accountRepository, consecutiveFailedLogins, eventsDispatcher, clock, failedLoginLockConfig)

    @Bean
    fun failedLoginLockAuthenticationListener(
        failedLoginLock: FailedLoginLock,
        currentHttpServletRequestService: CurrentHttpServletRequestService
    ) = FailedLoginLockAuthenticationListener(failedLoginLock, currentHttpServletRequestService)
}
