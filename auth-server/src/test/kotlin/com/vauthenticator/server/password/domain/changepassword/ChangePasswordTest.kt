package com.vauthenticator.server.password.domain.changepassword

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.ChangePasswordEvent
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import java.util.*

@ExtendWith(MockKExtension::class)
class ChangePasswordTest {

    lateinit var mokMvc: MockMvc

    val clientAppId = ClientAppId(A_CLIENT_APP_ID)

    private lateinit var underTest: ChangePassword

    @MockK
    lateinit var passwordPolicy: PasswordPolicy

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordEncoder: VAuthenticatorPasswordEncoder

    @MockK
    lateinit var eventsDispatcher: VAuthenticatorEventsDispatcher

    @BeforeEach
    fun setUp() {
        underTest = ChangePassword(
            eventsDispatcher,
            passwordPolicy,
            passwordEncoder,
            accountRepository
        )
    }

    @Test
    fun `when a change password attempt is executed`() {
        val account = anAccount()
        val principal = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            listOf(VAUTHENTICATOR_ADMIN),
            listOf(Scope.RESET_PASSWORD.content)
        )

        every { passwordPolicy.accept(EMAIL, "it is a new password") } just runs
        every { accountRepository.accountFor(principal.name) } returns Optional.of(account)
        every { passwordEncoder.encode("it is a new password") } returns "it is a encoded new password"
        every { accountRepository.save(account.copy(password = "it is a encoded new password")) } just runs
        every { eventsDispatcher.dispatch(any<ChangePasswordEvent>()) } just runs

        underTest.resetPasswordFor(principal, ChangePasswordRequest("it is a new password"))

        verify { passwordPolicy.accept(EMAIL, "it is a new password") }
        verify { accountRepository.accountFor(principal.name) }
        verify { passwordEncoder.encode("it is a new password") }
        verify { accountRepository.save(account.copy(password = "it is a encoded new password")) }
        verify { eventsDispatcher.dispatch(any<ChangePasswordEvent>()) }
    }

    @Test
    fun `when a user is not found`() {
        val principal = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            listOf(VAUTHENTICATOR_ADMIN),
            listOf(Scope.RESET_PASSWORD.content)
        )

        every { passwordPolicy.accept(EMAIL, "it is a new password") } just runs
        every { accountRepository.accountFor(principal.name) } returns Optional.empty()


        assertThrows(AccountNotFoundException::class.java) {
            underTest.resetPasswordFor(
                principal,
                ChangePasswordRequest("it is a new password")
            )
        }

        verify { passwordPolicy.accept(EMAIL, "it is a new password") }
        verify { accountRepository.accountFor(principal.name) }
    }

}