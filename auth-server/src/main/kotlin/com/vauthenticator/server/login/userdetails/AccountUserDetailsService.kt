package com.vauthenticator.server.login.userdetails

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.extentions.loginClientAppId
import com.vauthenticator.server.login.failedloginlock.FailedLoginLock
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

open class AccountUserDetailsService(
    private val userRepository: AccountRepository,
    private val failedLoginLock: FailedLoginLock,
    private val currentHttpServletRequestService: CurrentHttpServletRequestService
) : UserDetailsService {
    private val logger: Logger = LoggerFactory.getLogger(AccountUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): User =
        userRepository.accountFor(username)
            ?.let(::releaseExpiredFailedLoginLock)
            ?.let {
                logger.info("Account found for $username username")
                User(
                    it.username,
                    it.password,
                    it.enabled,
                    it.accountNonExpired,
                    it.credentialsNonExpired,
                    it.accountNonLocked,
                    it.authorities.map { SimpleGrantedAuthority(it) }
                )
            }
            ?: run {
                logger.error("Account not found for $username username")
                throw UsernameNotFoundException("the user $username not found")
            }

    private fun releaseExpiredFailedLoginLock(account: Account): Account =
        if (account.lockedUntil == null) {
            account
        } else {
            failedLoginLock.releaseIfExpired(
                account,
                currentHttpServletRequestService.getServletRequest().loginClientAppId()
            )
        }
}
