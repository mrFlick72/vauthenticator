package com.vauthenticator.server.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

class I18nMessageRepositoryTest {

    @Test
    fun `when the messages has errors in en`() {
        val uut = I18nMessageRepository(DefaultResourceLoader())

        val actual = uut.getMessagedFor(I18nScope.CHANGE_PASSWORD_PAGE, "en")
        val expected = I18nMessages(
            mapOf(
                "pageTitleText" to "Please change your password",
                "passwordPlaceholderText" to "New Password",
                "submitButtonTextReset" to "Change password"
            ),
            mapOf(
                "feedback" to "The new password does not meet the requirements"
            )
        )
        assertEquals(
            expected,
            actual
        )
    }

    @Test
    fun `when the messages doesn't has errors in en`() {
        val uut = I18nMessageRepository(DefaultResourceLoader())

        val actual = uut.getMessagedFor(I18nScope.SUCCESSFUL_MAIL_VERIFY_PAGE, "en")
        val expected = I18nMessages(
            mapOf(
                "pageTitleText" to "Confirmation of your email verification",
                "pageSuccessfulMessageText" to "Your email has been successfully verified."
            ),
            emptyMap()
        )
        assertEquals(
            expected,
            actual
        )
    }
}