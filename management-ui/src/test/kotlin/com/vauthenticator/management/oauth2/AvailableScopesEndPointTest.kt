package com.vauthenticator.management.oauth2

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vauthenticator.management.oauth2.ScopeFixture.scopes
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class AvailableScopesEndPointTest {

    lateinit var mokMvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @MockK
    lateinit var availableScopeRepository: AvailableScopeRepository

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(AvailableScopesEndPoint(availableScopeRepository))
            .build()
    }

    @Test
    fun `happy path`() {
        every { availableScopeRepository.getScopes() } returns scopes
        val scopesJsonContent = objectMapper.writeValueAsString(scopes)

        mokMvc.perform(get("/scopes"))
            .andExpect(status().isOk)
            .andExpect(content().string(scopesJsonContent))
    }
}