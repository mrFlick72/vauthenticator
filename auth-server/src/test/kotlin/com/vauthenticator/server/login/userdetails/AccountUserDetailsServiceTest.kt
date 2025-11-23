package com.vauthenticator.server.login.userdetails

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*

@ExtendWith(MockKExtension::class)
class AccountUserDetailsServiceTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    private val account = AccountTestFixture.anAccount()

    private lateinit var uut: AccountUserDetailsService

    @BeforeEach
    fun setUp() {
        uut = AccountUserDetailsService(accountRepository)
    }

    @Test
    fun `happy path`() {
        val expected = User(
            account.username,
            account.password,
            account.enabled,
            account.accountNonExpired,
            account.credentialsNonExpired,
            account.accountNonLocked,
            account.authorities.map { SimpleGrantedAuthority(it) }
        )
        every { accountRepository.accountFor(account.username) } returns Optional.of(account)

        val actual = uut.loadUserByUsername(account.username)
        assertEquals(expected, actual)
    }

    @Test
    fun `when the user does not exist`() {
        every { accountRepository.accountFor(account.username) } returns Optional.empty()

        assertThrows(UsernameNotFoundException::class.java) { uut.loadUserByUsername(account.username) }
    }
}