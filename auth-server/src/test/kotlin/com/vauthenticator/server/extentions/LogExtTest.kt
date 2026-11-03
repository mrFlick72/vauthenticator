package com.vauthenticator.server.extentions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LogExtTest {

    @Test
    fun `line breaks are escaped so they cannot forge log lines`() {
        assertEquals("x\\r\\nThe user admin", "x\r\nThe user admin".escapedForLog())
    }

    @Test
    fun `a literal backslash is escaped so it cannot pass for an escaped line break`() {
        assertEquals("x\\\\nThe user admin", "x\\nThe user admin".escapedForLog())
    }

    @Test
    fun `control characters and unicode line separators are escaped`() {
        assertEquals(
            "a\\u0085b\\u2028c\\u2029d\\u001b[2Je",
            "a\u0085b c d\u001B[2Je".escapedForLog()
        )
    }

    @Test
    fun `bidi and invisible format characters are escaped so they cannot disguise a value`() {
        assertEquals(
            "admin\\u202egro.niamod\\u200b\\u2066",
            "admin‮gro.niamod​⁦".escapedForLog()
        )
    }

    @Test
    fun `ordinary text is left as it is`() {
        assertEquals("Mozilla/5.0 (X11; Linux x86_64) é 😀", "Mozilla/5.0 (X11; Linux x86_64) é 😀".escapedForLog())
    }
}
