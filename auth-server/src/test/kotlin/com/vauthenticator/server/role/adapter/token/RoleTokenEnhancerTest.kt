package com.vauthenticator.server.role.adapter.token

import com.vauthenticator.server.support.JwtEncodingContextFixture
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RoleTokenEnhancerTest {

    @Test
    fun `when the roles are put in an access token claims`() {
        val uut = RoleTokenEnhancer("access_token", "roles")
        val context = JwtEncodingContextFixture.newContext

        uut.customize(context)

        val expected = listOf("USER")
        val actual = context.claims.build().claims["roles"]

        assertEquals(expected, actual)
    }


    @Test
    fun `when the roles are put in an id token claims`() {
        val uut = RoleTokenEnhancer("id_token", "roles")
        val context = JwtEncodingContextFixture.newIdTokenContext

        uut.customize(context)

        val expected = listOf("USER")
        val actual = context.claims.build().claims["roles"]

        assertEquals(expected, actual)
    }

    @Test
    fun `when token is access token but authorization is null then the roles claims will be not added`() {
        val uut = RoleTokenEnhancer("access_token", "roles")
        val context = JwtEncodingContextFixture.newContextWithoutAuthorization

        uut.customize(context)

        assertThrows(IllegalArgumentException::class.java, {
            context.claims.build().claims["roles"] as List<*>
        }, "claims cannot be empty")
    }

    @Test
    fun `when the roles are not put in any token since that the principal is a client credential principal`() {
        val uut = RoleTokenEnhancer("access_token", "roles")
        val context = JwtEncodingContextFixture.newClientCredentialsContext

        uut.customize(context)

        assertThrows(IllegalArgumentException::class.java, {
            context.claims.build().claims["roles"] as List<*>
        }, "claims cannot be empty")
    }
}
