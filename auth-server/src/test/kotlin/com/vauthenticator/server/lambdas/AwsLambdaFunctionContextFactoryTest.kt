package com.vauthenticator.server.lambdas

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.JwtEncodingContextFixture.newContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class AwsLambdaFunctionContextFactoryTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @Test
    fun `happy path`() {
        val anAccount = anAccount()
        every { accountRepository.accountFor(EMAIL) } returns Optional.of(anAccount)
        val uut = AwsLambdaFunctionContextFactory(accountRepository)

        val emptyMap = emptyMap<String, Any>()
        val expected = LambdaFunctionContext(
            mapOf(
                "user" to mapOf(
                    "sub" to anAccount.email.toSha256(),
                    "email" to anAccount.email,
                    "first_name" to anAccount.firstName,
                    "last_name" to anAccount.lastName,
                    "phone" to "",
                    "birth_date" to "",
                    "email_verified" to false,
                    "roles" to emptySet<String>()

                ),
                "general_context_claims" to mapOf(
                    "client_id" to "client_id",
                    "grant_flow" to "authorization_code",
                    "authorized_scope" to  emptySet<String>()
                ),
                "access_token_claims" to emptyMap,
                "id_token_claims" to emptyMap
            )
        )
        val actual = uut.newLambdaFunctionContext(newContext)

        assertEquals(expected, actual)
    }

}