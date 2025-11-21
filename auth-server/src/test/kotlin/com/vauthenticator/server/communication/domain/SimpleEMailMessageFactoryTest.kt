package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.support.AccountTestFixture.anAccount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NEW_ACCOUNT_EMAIL_EMAIL_COM = "new-accountmail@email.com"

internal class SimpleEMailMessageFactoryTest {

    private val underTest = SimpleEMailMessageFactory("from", "subject", EMailType.WELCOME)

    @Test
    internal fun `make a new mail message`() {
        val account = anAccount()
        val actual = underTest.makeMailMessageFor(account, mapOf("key" to "value"))

        val expected = EMailMessage(
                "email@domain.com", "from", "subject", EMailType.WELCOME,
                mapOf(
                        "enabled" to account.enabled,
                        "username" to account.username,
                        "authorities" to account.authorities,
                        "email" to account.email,
                        "firstName" to account.firstName,
                        "lastName" to account.lastName,
                        "birthDate" to "",
                        "phone" to "",
                        "key" to "value"
                )
        )

        assertEquals(expected, actual)
    }

    @Test
    internal fun `make a new mail message when the sender mail has changed`() {
        val account = anAccount()
        val actual = underTest.makeMailMessageFor(account, mapOf("key" to "value", "email" to NEW_ACCOUNT_EMAIL_EMAIL_COM))

        val expected = EMailMessage(
            NEW_ACCOUNT_EMAIL_EMAIL_COM, "from", "subject", EMailType.WELCOME,
                mapOf(
                        "enabled" to account.enabled,
                        "username" to account.username,
                        "authorities" to account.authorities,
                        "email" to NEW_ACCOUNT_EMAIL_EMAIL_COM,
                        "firstName" to account.firstName,
                        "lastName" to account.lastName,
                        "birthDate" to "",
                        "phone" to "",
                        "key" to "value"
                )
        )

        assertEquals(expected, actual)
    }

}