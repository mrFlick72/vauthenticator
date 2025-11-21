package com.vauthenticator.server.login.userdetails

import com.vauthenticator.server.account.domain.AccountRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

open class AccountUserDetailsService(private val userRepository: AccountRepository) : UserDetailsService {
    private val logger: Logger = LoggerFactory.getLogger(AccountUserDetailsService::class.java)

    override fun loadUserByUsername(username: String): User =
            userRepository.accountFor(username)
                    .map {
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
                    .orElseThrow {
                        logger.error("Account not found for $username username")
                        UsernameNotFoundException("the user $username not found")
                    }
}