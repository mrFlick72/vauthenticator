package com.vauthenticator.server.oauth2.clientapp.ext

import com.vauthenticator.server.oauth2.clientapp.domain.Secret
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient

fun RegisteredClient.isConfidential() =
    !this.clientAuthenticationMethods.contains(ClientAuthenticationMethod.NONE)


fun RegisteredClient.getClientSecretSafely(): Secret {
    return if (isConfidential()) {
        Secret(requireNotNull(clientSecret) {
            "Confidential client $clientId must define a clientSecret"
        })
    } else {
        Secret("")
    }
}