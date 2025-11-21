package com.vauthenticator.server.mfa.api

import com.vauthenticator.server.mfa.domain.MfaChallengeSender
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class MfaChallengeEndPoint(
    private val permissionValidator: PermissionValidator,
    private val mfaChallengeSender: MfaChallengeSender
) {

    @PutMapping("/api/mfa/challenge")
    fun sendMfaChallenge(
        authentication: Authentication,
        httpSession: HttpSession,
        @RequestParam("mfa-device-id", required = false) mfaDeviceId: Optional<String>,
    ) {
        when (authentication) {
            is JwtAuthenticationToken -> permissionValidator.validate(authentication, Scopes.from(Scope.MFA_ALWAYS))
            else -> permissionValidator.validate(null, httpSession, Scopes.from(Scope.MFA_ALWAYS))
        }

        mfaDeviceId.ifPresentOrElse(
            { mfaChallengeSender.sendMfaChallengeFor(authentication.name, MfaDeviceId(it)) },
            { mfaChallengeSender.sendMfaChallengeFor(authentication.name) }
        )
    }

}