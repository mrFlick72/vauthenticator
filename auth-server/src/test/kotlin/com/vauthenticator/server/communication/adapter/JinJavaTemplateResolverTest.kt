package com.vauthenticator.server.communication.adapter

import com.hubspot.jinjava.Jinjava
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JinJavaTemplateResolverTest {
    @Test
    internal fun `happy path`() {
        val jinjavaMailTemplateResolver = JinJavaTemplateResolver(Jinjava())

        val expected = "Hello Jho!"
        val actual = jinjavaMailTemplateResolver.compile("Hello {{ name }}!", mapOf("name" to "Jho"))

        assertEquals(expected, actual)
    }
}