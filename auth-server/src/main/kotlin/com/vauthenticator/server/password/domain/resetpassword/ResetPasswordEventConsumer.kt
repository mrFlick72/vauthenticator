package com.vauthenticator.server.password.domain.resetpassword

import com.vauthenticator.platform.events.EventConsumer
import com.vauthenticator.server.events.ResetPasswordEvent
import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import org.springframework.stereotype.Service

class ResetPasswordEventConsumer(
    private val passwordHistoryRepository: PasswordHistoryRepository
) : EventConsumer<VAuthenticatorEvent> {
    override fun accept(event: VAuthenticatorEvent) {
        passwordHistoryRepository.store(event.principal, event.payload as Password)
    }

    override fun handleable(event: VAuthenticatorEvent): Boolean = event is ResetPasswordEvent
}