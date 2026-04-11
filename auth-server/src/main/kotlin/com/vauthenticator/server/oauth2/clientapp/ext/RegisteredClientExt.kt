package com.vauthenticator.server.oauth2.clientapp.ext

import com.vauthenticator.server.oauth2.clientapp.domain.InvalidAppDataException
import com.vauthenticator.server.oauth2.clientapp.domain.Secret
import com.vauthenticator.server.web.ValidationResult
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import kotlin.jvm.Throws

fun RegisteredClient.isConfidential() =
    !this.clientAuthenticationMethods.contains(ClientAuthenticationMethod.NONE)


/**
 * Returns the client secret as a domain [Secret].
 *
 * @throws InvalidAppDataException when the client is confidential and no secret is defined.
 */
@Throws(InvalidAppDataException::class)
fun RegisteredClient.clientSecret(): Secret =
    if (isConfidential()) {
        Secret(clientSecret ?: throw missingClientSecretException(clientId))
    } else {
        Secret("")
    }

private fun missingClientSecretException(clientId: String): InvalidAppDataException =
    InvalidAppDataException.exceptionFrom(
        mutableMapOf(
            "client_application.secret" to ValidationResult(
                errorMessage = "Client app $clientId secret is empty or blank and it is not supported for confidential client applications",
                errorsCode = listOf(
                    "client_application.secret.empty",
                    "client_application.secret.blank"
                )
            )
        )
    )
