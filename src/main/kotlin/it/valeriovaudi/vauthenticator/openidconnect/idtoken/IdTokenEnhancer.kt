package it.valeriovaudi.vauthenticator.openidconnect.idtoken

import it.valeriovaudi.vauthenticator.keypair.KeyRepository
import it.valeriovaudi.vauthenticator.time.Clock
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.TokenEnhancer

class IdTokenEnhancer(private val oidcIss: String,
                      private val keyRepository: KeyRepository,
                      private val clock: Clock) : TokenEnhancer {

    override fun enhance(accessToken: OAuth2AccessToken, authentication: OAuth2Authentication): OAuth2AccessToken {
        val defaultAccessToken = accessToken as DefaultOAuth2AccessToken

        val additionalInformation = defaultAccessToken.additionalInformation

        addIdTokenFor(additionalInformation, authentication, defaultAccessToken)

        return defaultAccessToken
    }

    private fun addIdTokenFor(additionalInformation: MutableMap<String, Any>,
                              authentication: OAuth2Authentication,
                              defaultAccessToken: DefaultOAuth2AccessToken) {

        if (defaultAccessToken.scope.contains("openid")) {
            additionalInformation["id_token"] = idTokenAsJwt(authentication)
            defaultAccessToken.additionalInformation = additionalInformation
        }

    }

    private fun idTokenAsJwt(authentication: OAuth2Authentication): String {
        val keyPair = keyRepository.getKeyPair()
        val idToken = IdToken.createIdToken(oidcIss, authentication.name, authentication, clock)
        val idTokenAsJwtSignedFor = idToken.idTokenAsJwtSignedFor(keyPair)
        return idTokenAsJwtSignedFor
    }

}