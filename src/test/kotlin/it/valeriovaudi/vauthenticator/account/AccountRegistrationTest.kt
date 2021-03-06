package it.valeriovaudi.vauthenticator.account

import it.valeriovaudi.vauthenticator.extentions.VAuthenticatorPasswordEncoder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AccountRegistrationTest {

    private val account = AccountTestFixture.anAccount()

    @Mock
    lateinit var accountRepository: AccountRepository

    @Mock
    lateinit var passwordEncoder: VAuthenticatorPasswordEncoder

    @Mock
    lateinit var eventPublisher: AccountRegistrationEventPublisher

    @Test
    fun `register a new user`() {
        val accountRegistration = AccountRegistration(accountRepository, passwordEncoder, eventPublisher)
        given(passwordEncoder.encode(account.password))
                .willReturn(account.password)

        accountRegistration.execute(account)

        verify(accountRepository).save(account)
        verify(eventPublisher).accountCreated(
                AccountCreated(email = account.email,
                        firstName = account.firstName,
                        lastName = account.lastName)
        )
    }
}