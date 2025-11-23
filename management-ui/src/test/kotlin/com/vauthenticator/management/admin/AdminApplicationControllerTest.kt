package com.vauthenticator.management.admin


import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@WebMvcTest(AdminApplicationController::class)
class AdminApplicationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(authorities = ["VAUTHENTICATOR_ADMIN"])
    fun `happy path`() {
        mockMvc.perform(get("/secure/admin/index"))
            .andExpect(status().isOk)
            .andExpect(view().name("template"))
    }
}