package com.vauthenticator.server.lambdas

import com.vauthenticator.server.account.domain.AccountRepository
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.util.*


class LambdaTokenEnhancer(
    private val enabled: Boolean,
    private val lambdaName: String,
    private val lambdaFunction: LambdaFunction,
    private val lambdaFunctionContextFactory: LambdaFunctionContextFactory<JwtEncodingContext>
) : OAuth2TokenCustomizer<JwtEncodingContext> {


    override fun customize(context: JwtEncodingContext) {
        if (enabled) {
            val tokenType = context.tokenType.value

            val lambdaFunctionId = LambdaFunctionId(lambdaName)
            val lambdaFunctionContext = lambdaFunctionContextFactory.newLambdaFunctionContext(context)

            val execute = lambdaFunction.execute(lambdaFunctionId, lambdaFunctionContext)
            if ("id_token" === tokenType) {
                (execute.content["id_token_claims"] as Map<String, String>)
                    .map { claim -> context.claims.claim(claim.key, claim.value) }

            } else if ("access_token" === tokenType) {
                (execute.content["access_token_claims"] as Map<String, String>)
                    .map { claim -> context.claims.claim(claim.key, claim.value) }
            }
        }
    }
}

class AwsLambdaFunctionContextFactory(private val accountRepository: AccountRepository) :
    LambdaFunctionContextFactory<JwtEncodingContext> {
    override fun newLambdaFunctionContext(input: JwtEncodingContext): LambdaFunctionContext {
        val clientId = input.registeredClient.clientId
        val grantFlow = input.authorizationGrantType.value
        val authorizedScope = input.authorizedScopes
        val userContext = mutableMapOf<String, Any>()
        val generalContext = mutableMapOf<String, Any>()

        generalContext["client_id"] = clientId
        generalContext["grant_flow"] = grantFlow
        generalContext["authorized_scope"] = authorizedScope

        Optional.ofNullable(input.authorization)
            .map {
                accountRepository.accountFor(it.principalName)
                    .map { account ->
                        userContext["sub"] = account.sub
                        userContext["email"] = account.email
                        userContext["first_name"] = account.firstName
                        userContext["last_name"] = account.lastName
                        userContext["birth_date"] = account.birthDate.map { it.formattedDate() }.orElseGet { "" }
                        userContext["phone"] = account.phone.map { it.formattedPhone() }.orElseGet { "" }
                        userContext["email_verified"] = account.emailVerified
                        userContext["roles"] = account.authorities
                    }
            }

        val accessTokenContext = mutableMapOf<String, String>()
        val idTokenContext = mutableMapOf<String, String>()

        return LambdaFunctionContext(
            mapOf(
                "user" to userContext,
                "general_context_claims" to generalContext,
                "access_token_claims" to accessTokenContext,
                "id_token_claims" to idTokenContext
            )
        )
    }

}