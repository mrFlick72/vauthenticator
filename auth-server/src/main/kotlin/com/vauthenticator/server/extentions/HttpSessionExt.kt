package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpSession
import org.springframework.security.web.savedrequest.DefaultSavedRequest

fun HttpSession.oauth2ClientId(): ClientAppId? =
    when (val savedRequest = getAttribute("SPRING_SECURITY_SAVED_REQUEST")) {
        is DefaultSavedRequest -> clientIdFromSessionWithinA(savedRequest)
        else -> null
    }?.let(::ClientAppId)
        ?: (getAttribute("clientId") as String?)?.let(::ClientAppId)


//TODO boyscout improve it
private fun clientIdFromSessionWithinA(defaultSavedRequest: DefaultSavedRequest): String? =
    if (defaultSavedRequest.parameterNames.contains("client_id")) {
        defaultSavedRequest.getParameterValues("client_id")?.firstOrNull()
    } else {
        null
    }
