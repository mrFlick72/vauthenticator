package com.vauthenticator.server.password.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.password.domain.PasswordGenerator
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@ExtendWith(MockKExtension::class)
class PasswordGeneratorEndPointTest {

    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var passwordGenerator: PasswordGenerator

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            PasswordGeneratorEndPoint(passwordGenerator)
        ).build()
    }

    @Test
    fun `when a new random password is generated`() {
        every { passwordGenerator.generate() } returns "A_RANDOM_PASSWORD"
        val expected = GeneratedPasswordResponse("A_RANDOM_PASSWORD")

        mokMvc.perform(post("/api/password"))
            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
            .andExpect(status().isOk)

        verify { passwordGenerator.generate() }
    }
}
