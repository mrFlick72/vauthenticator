package com.vauthenticator.server.account.domain.welcome

import com.vauthenticator.platform.events.EventConsumer
import com.vauthenticator.server.events.SignUpEvent
import com.vauthenticator.platform.events.VAuthenticatorEvent

class SendWelcomeMailUponSignUpEventConsumer(
    private val sayWelcome: SayWelcome
) : EventConsumer<VAuthenticatorEvent> {
    override fun accept(event: VAuthenticatorEvent) {
        sayWelcome.welcome(event.principal)
    }

    override fun handleable(event: VAuthenticatorEvent): Boolean = event is SignUpEvent

}