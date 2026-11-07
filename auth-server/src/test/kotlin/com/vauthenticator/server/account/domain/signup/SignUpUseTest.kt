package com.vauthenticator.server.account.domain.signup

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.events.SignUpEvent
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.AdministrationFixture.accountAdministrationFor
import com.vauthenticator.server.support.AdministrationFixture.notAnAdministrator
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
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class SignUpUseTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordPolicy: PasswordPolicy

    @MockK
    lateinit var vAuthenticatorPasswordEncoder: VAuthenticatorPasswordEncoder

    @MockK
    lateinit var vAuthenticatorEventsDispatcher: VAuthenticatorEventsDispatcher

    private lateinit var underTest: SignUpUse

    @BeforeEach
    internal fun setUp() {
        underTest = SignUpUse(
            passwordPolicy,
            accountRepository,
            accountAdministrationFor(notAnAdministrator),
            vAuthenticatorPasswordEncoder,
            vAuthenticatorEventsDispatcher
        )
    }

    @Test
    internal fun `when a new account is created`() {
        val now = Instant.now()
        val clientAppId = ClientAppId("an_id")
        val account = anAccount().copy(
            authorities = setOf(Role.defaultRole().name),
            password = "encrypted_secret"
        )

        val signUpEvent = SignUpEvent(Email(account.email), clientAppId, now, Password("encrypted_secret"))

        every { passwordPolicy.accept(account.email, "secret") } just runs
        every { vAuthenticatorPasswordEncoder.encode("secret") } returns "encrypted_secret"
        every { accountRepository.create(account) } just runs
        every { vAuthenticatorEventsDispatcher.dispatch(signUpEvent) } just runs


        underTest.execute(clientAppId, account.copy(password = "secret"))

        verify { passwordPolicy.accept(account.email, "secret") }
        verify { vAuthenticatorPasswordEncoder.encode("secret") }
        verify { accountRepository.create(account) }
        verify { vAuthenticatorEventsDispatcher.dispatch(signUpEvent) }
    }

    @Test
    internal fun `a caller that is not an administrator cannot sign up an account with the administrator role`() {
        val account = anAccount().copy(authorities = setOf(Role.adminRole().name), password = "secret")
        every { passwordPolicy.accept(account.email, "secret") } just runs
        every { vAuthenticatorPasswordEncoder.encode("secret") } returns "encrypted_secret"

        assertThrows(AdministrationRequiredException::class.java) {
            underTest.execute(ClientAppId("an_id"), account)
        }

        verify(exactly = 0) { accountRepository.create(any()) }
        verify(exactly = 0) { vAuthenticatorEventsDispatcher.dispatch(any()) }
    }
}
