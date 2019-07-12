package it.valeriovaudi.vauthenticator.openidconnect.userinfo

import it.valeriovaudi.TestAdditionalConfiguration
import it.valeriovaudi.vauthenticator.support.WithMockOAuth2User
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper

@RunWith(SpringRunner::class)
@ActiveProfiles("web-tier")
@WebMvcTest(UserInfoEndPoint::class)
@Import(TestAdditionalConfiguration::class)
class UserInfoEndPointTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    @WithMockOAuth2User("A_USER_NAME")
    fun `happy path`() {
        val objectMapper = ObjectMapper()

        mockMvc.perform(get("/user-info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(UserInfo("A_USER_NAME", "A_USER_NAME", listOf("USER")))))

    }
}