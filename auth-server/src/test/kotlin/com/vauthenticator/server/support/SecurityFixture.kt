package com.vauthenticator.server.support

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.time.Instant

const val VAUTHENTICATOR_ADMIN = "VAUTHENTICATOR_ADMIN"

object SecurityFixture {

    private val key: ECKey = ECKeyGenerator(Curve.P_256)
        .keyID("123")
        .generate()

    fun simpleJwtFor(clientAppId: String, email: String = ""): String {
        val signedJWT = signedJWTFor(clientAppId, email)
        return signedJWT.serialize()
    }

    fun signedJWTFor(clientAppId: String, email: String, scopes: List<String> = emptyList()): SignedJWT {
        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .type(JOSEObjectType.JWT)
            .keyID("123")
            .build();


        var claim = JWTClaimsSet.Builder()
            .claim(IdTokenClaimNames.AZP, clientAppId)
            .claim(IdTokenClaimNames.AUD, clientAppId)
            .claim("scope", scopes)

        if (email.isNotBlank()) {
            claim = claim.claim("user_name", email)
        }

        val payload = claim.build()

        val signedJWT = SignedJWT(header, payload)
        signedJWT.sign(ECDSASigner(key.toECPrivateKey()))
        return signedJWT
    }

    fun principalFor(
        clientAppId: String,
        email: String,
        authorities: List<String> = emptyList(),
        scopes: List<String> = emptyList()
    ) =
        signedJWTFor(clientAppId, email, scopes).let { signedJWT ->
            JwtAuthenticationToken(
                Jwt(
                    simpleJwtFor(clientAppId),
                    Instant.now(),
                    Instant.now().plusSeconds(100),
                    signedJWT.header.toJSONObject(),
                    signedJWT.payload.toJSONObject()
                ),
                authorities.map(::SimpleGrantedAuthority),
                email
            )
        }

    fun m2mPrincipalFor(
        clientAppId: String,
        scopes: List<String> = emptyList()
    ) =
        signedJWTFor(clientAppId, "", scopes).let { signedJWT ->
            JwtAuthenticationToken(
                Jwt(
                    simpleJwtFor(clientAppId),
                    Instant.now(),
                    Instant.now().plusSeconds(100),
                    signedJWT.header.toJSONObject(),
                    signedJWT.payload.toJSONObject()
                )
            )
        }

    fun principalFor(mail: String, authorities: List<String> = listOf("USER")): UsernamePasswordAuthenticationToken =
        UsernamePasswordAuthenticationToken.authenticated(mail, "", authorities.map(::SimpleGrantedAuthority))

}