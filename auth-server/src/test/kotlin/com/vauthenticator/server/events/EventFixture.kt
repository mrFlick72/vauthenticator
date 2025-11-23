package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.ClientAppFixture
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import java.time.Instant

object EventFixture {
    val vauthenticatorAuthEvent = VAuthenticatorAuthEvent(
        Email("anemail@domain.com"),
        ClientAppId(A_CLIENT_APP_ID),
        Instant.now(),
        AuthenticationSuccessEvent(UsernamePasswordAuthenticationToken("anemail@domain.com", "*******"))
    )

    val signUpEvent = SignUpEvent(
        Email("AN_EMAIL"),
        ClientAppFixture.aClientAppId(),
        Instant.now(),
        Password("A_PASSWORD")
    )

}