package it.valeriovaudi.vauthenticator.account

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AccountRegistrationTest {

    private val sub = UUID.randomUUID().toString()
    private val account = AccountTestFixture.anAccount(sub)

    @Mock
    lateinit var accountRepository: AccountRepository

    @Mock
    lateinit var passwordEncoder: AccountPasswordEncoder

    @Mock
    lateinit var eventPublisher: AccountRegistrationEventPublisher

    @Test
    fun `register a new user`() {
        val accountRegistration = AccountRegistration(accountRepository, passwordEncoder, eventPublisher)
        given(passwordEncoder.encode(account.password))
                .willReturn(account.password)

        accountRegistration.execute(account)

        verify(accountRepository).create(account)
        verify(eventPublisher).accountCreated(
                AccountCreated(email = account.email,
                        firstName = account.firstName,
                        lastName = account.lastName)
        )
    }
}