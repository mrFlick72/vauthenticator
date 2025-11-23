package com.vauthenticator.server.role.adapter.token

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.JwtEncodingContextFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
class GroupTokenEnhancerTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @Test
    fun `when the groups are put in an access token claims`() {
        val uut = GroupTokenEnhancer("access_token", "groups", accountRepository)
        val context = JwtEncodingContextFixture.newContext

        every { accountRepository.accountFor(EMAIL) } returns Optional.of(anAccount().copy(groups = setOf("A_GROUP")))

        uut.customize(context)

        verify { accountRepository.accountFor(EMAIL) }

        val expected = setOf("A_GROUP")
        val actual = context.claims.build().claims["groups"]

        assertEquals(expected, actual)
    }


    @Test
    fun `when the groups are put in an id token claims`() {
        val uut = GroupTokenEnhancer("id_token", "groups", accountRepository)
        val context = JwtEncodingContextFixture.newIdTokenContext

        every { accountRepository.accountFor(EMAIL) } returns Optional.of(anAccount().copy(groups = setOf("A_GROUP")))

        uut.customize(context)

        verify { accountRepository.accountFor(EMAIL) }

        val expected = setOf("A_GROUP")
        val actual = context.claims.build().claims["groups"]

        assertEquals(expected, actual)
    }


    @Test
    fun `when the groups are not put in any token since that the principal is a client credential principal`() {
        val uut = GroupTokenEnhancer("id_token", "groups", accountRepository)
        val context = JwtEncodingContextFixture.newClientCredentialsContext

        uut.customize(context)

        assertThrows(IllegalArgumentException::class.java, {
            context.claims.build().claims["groups"] as List<*>
        }, "claims cannot be empty")
    }
}