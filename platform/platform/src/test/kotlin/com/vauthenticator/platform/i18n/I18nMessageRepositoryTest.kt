package com.vauthenticator.platform.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader

class I18nMessageRepositoryTest {

    private val uut = I18nMessageRepository(DefaultResourceLoader())

    @Test
    fun `the messages and errors of a scope come from the bundle of the language`() {
        val actual = uut.getMessagedFor("a_page", "it")

        assertEquals(I18nMessages(mapOf("title" to "Un titolo", "button" to "Invia"), emptyMap()), actual)
    }

    @Test
    fun `a language without a bundle falls back to the default bundle`() {
        val actual = uut.getMessagedFor("a_page", "fr")

        assertEquals(
            I18nMessages(
                mapOf("title" to "A title", "button" to "Send"),
                mapOf("feedback" to "Something went wrong")
            ),
            actual
        )
    }

    @Test
    fun `only the keys of the scope are returned`() {
        val actual = uut.getMessagedFor("another_page", "en")

        assertEquals(I18nMessages(mapOf("title" to "Another title"), emptyMap()), actual)
    }
}
