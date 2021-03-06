package it.valeriovaudi.vauthenticator.jwk

import it.valeriovaudi.TestAdditionalConfiguration
import it.valeriovaudi.vauthenticator.keypair.KeyPairFixture.getFileContent
import it.valeriovaudi.vauthenticator.keypair.KeyPairFixture.keyPair
import it.valeriovaudi.vauthenticator.keypair.KeyRepository
import it.valeriovaudi.vauthenticator.openid.connect.nonce.NonceStore
import it.valeriovaudi.vauthenticator.security.userdetails.AccountUserDetailsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@TestPropertySource(properties = ["key-store.keyStorePairAlias=ALIAS"])
@Import(TestAdditionalConfiguration::class)
@WebMvcTest(JwksEndPoint::class)
class JwksEndPointTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var keyRepository: KeyRepository

    @MockBean
    lateinit var jwkFactory: JwkFactory

    @MockBean
    lateinit var nonceStore: NonceStore

    @MockBean
    lateinit var redisTemplate: RedisTemplate<*, *>

    @MockBean
    lateinit var accountUserDetailsService: AccountUserDetailsService

    @Test
    fun `happy path`() {
        val content = getFileContent("/keystore/keystore.jks")
        val keyPair = keyPair(content)

        given(keyRepository.getKeyPair())
                .willReturn(keyPair)

        given(jwkFactory.createJwks(keyPair, "ALIAS"))
                .willReturn(emptyMap())

        mockMvc.perform(MockMvcRequestBuilders.get("/.well-known/jwks.json"))
                .andExpect(status().isOk)

        verify(keyRepository).getKeyPair()
        verify(jwkFactory).createJwks(keyPair, "ALIAS")
    }
}