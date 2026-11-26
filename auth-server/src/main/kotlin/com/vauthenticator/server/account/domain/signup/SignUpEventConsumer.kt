package com.vauthenticator.server.account.domain.signup

import com.vauthenticator.server.events.EventConsumer
import com.vauthenticator.server.events.SignUpEvent
import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import org.springframework.stereotype.Service

@Service
class SignUpEventConsumer(
    private val passwordHistoryRepository: PasswordHistoryRepository
) : EventConsumer {
    override fun accept(event: VAuthenticatorEvent) {
        passwordHistoryRepository.store(event.principal, event.payload as Password)
    }

    override fun handleable(event: VAuthenticatorEvent): Boolean = event is SignUpEvent
}