package com.vauthenticator.server.oauth2.token

import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.Keys
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.JwtEncodingContextFixture
import com.vauthenticator.server.support.KeysUtils.aKeyFor
import com.vauthenticator.server.support.KeysUtils.aKid
import com.vauthenticator.server.support.KeysUtils.aMasterKey
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OAuth2TokenEnhancerTest {

    @MockK
    private lateinit var keyRepository: KeyRepository

    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    lateinit var uut : OAuth2TokenEnhancer

    @BeforeEach
    fun setUp() {
        uut = OAuth2TokenEnhancer(
            mutableSetOf(),
            keyRepository,
            clientApplicationRepository
        )    }

    @Test
    fun `when token is access token and grant type is not client credentials then claims and kid are enriched from context and signing key`() {
        val context = JwtEncodingContextFixture.newContext
        val signingKey = aKeyFor(aMasterKey.content(), aKid.content())

        every { keyRepository.signatureKeys() } returns Keys(listOf(signingKey))

        uut.customize(context)

        verify(exactly = 1) { keyRepository.signatureKeys() }
        verify(exactly = 0) { clientApplicationRepository.findOne(any()) }
        assertEquals(EMAIL, context.claims.build().claims["user_name"])
        assertEquals(aKid.content(), context.jwsHeader.build().headers["kid"])
    }

    @Test
    fun `when token is access token and grant type is not client credentials but authorization is null so the user_name claims will be not added`() {
        val context = JwtEncodingContextFixture.newContextWithoutAuthorization
        val signingKey = aKeyFor(aMasterKey.content(), aKid.content())

        every { keyRepository.signatureKeys() } returns Keys(listOf(signingKey))

        uut.customize(context)

        verify(exactly = 1) { keyRepository.signatureKeys() }
        verify(exactly = 0) { clientApplicationRepository.findOne(any()) }
        assertEquals(aKid.content(), context.jwsHeader.build().headers["kid"])

        assertThrows(IllegalArgumentException::class.java) { context.claims.build().claims["user_name"] }
    }
}
