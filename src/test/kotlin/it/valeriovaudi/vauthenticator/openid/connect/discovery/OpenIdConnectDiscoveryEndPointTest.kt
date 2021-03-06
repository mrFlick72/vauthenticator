package it.valeriovaudi.vauthenticator.openid.connect.discovery

import com.fasterxml.jackson.databind.ObjectMapper
import it.valeriovaudi.TestAdditionalConfiguration
import it.valeriovaudi.vauthenticator.openid.connect.discovery.OpenIdConnectDiscovery.Companion.newOpenIdConnectDiscovery
import it.valeriovaudi.vauthenticator.security.userdetails.AccountUserDetailsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content

@ExtendWith(SpringExtension::class)
@ActiveProfiles("web-tier")
@WebMvcTest(OpenIdConnectDiscoveryEndPoint::class)
@Import(TestAdditionalConfiguration::class)
@TestPropertySource(properties = ["auth.oidcIss=anIssuer"])
class OpenIdConnectDiscoveryEndPointTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var accountUserDetailsService: AccountUserDetailsService

    @Test
    fun `happy path`() {
        val objectMapper = ObjectMapper()
        mockMvc.perform(get("/.well-known/openid-configuration"))
                .andExpect(content().json(objectMapper.writeValueAsString(newOpenIdConnectDiscovery("anIssuer"))))
    }
}