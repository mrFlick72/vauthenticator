package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.events.EventConsumer
import com.vauthenticator.server.events.SignUpEvent
import com.vauthenticator.platform.events.VAuthenticatorEvent

class SendVerifyEMailChallengeUponSignUpEventConsumer(
    private val mailChallenge: SendVerifyEMailChallenge
) : EventConsumer {
    override fun accept(event: VAuthenticatorEvent) {
        mailChallenge.sendVerifyMail(event.principal)
    }

    override fun handleable(event: VAuthenticatorEvent): Boolean = event is SignUpEvent

}