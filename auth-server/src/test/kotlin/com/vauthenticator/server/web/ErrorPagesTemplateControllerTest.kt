package com.vauthenticator.server.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class ErrorPagesTemplateControllerTest {

    lateinit var mokMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mokMvc = standaloneSetup(ErrorPagesTemplateController(objectMapper)).build()
    }

    @Test
    fun `when a 404 happen`() {
        mokMvc.perform(get("/page-not-found"))
            .andExpect(status().isNotFound)
    }

}