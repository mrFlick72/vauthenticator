package com.vauthenticator.server.lambdas

import com.vauthenticator.server.support.JwtEncodingContextFixture.newContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext

@ExtendWith(MockKExtension::class)
class LambdaTokenEnhancerTest {

    @MockK
    private lateinit var lambdaFunction: LambdaFunction

    @MockK
    private lateinit var lambdaFunctionContextFactory: LambdaFunctionContextFactory<JwtEncodingContext>

    private val context = LambdaFunctionContext.empty()

    @Test
    fun `when the lambda is not applied`() {
        val uut = LambdaTokenEnhancer(false, "A_LAMBDA_NAME", lambdaFunction, lambdaFunctionContextFactory)

        uut.customize(newContext)

        verify(exactly = 0) { lambdaFunction.execute(any(), any()) }
    }

    @Test
    fun `when the lambda is applied`() {
        val uut = LambdaTokenEnhancer(true, "A_LAMBDA_NAME", lambdaFunction, lambdaFunctionContextFactory)

        every { lambdaFunctionContextFactory.newLambdaFunctionContext(newContext) } returns context
        every { lambdaFunction.execute(LambdaFunctionId("A_LAMBDA_NAME"), context) } returns context

        uut.customize(newContext)

        verify(exactly = 1) { lambdaFunction.execute(LambdaFunctionId("A_LAMBDA_NAME"), context) }
    }
}