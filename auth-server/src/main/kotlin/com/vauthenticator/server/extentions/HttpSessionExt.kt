package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpSession
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import java.util.*
import java.util.Optional.empty
import java.util.Optional.ofNullable

fun HttpSession.oauth2ClientId(): Optional<ClientAppId> =
     ofNullable(getAttribute("SPRING_SECURITY_SAVED_REQUEST"))
        .flatMap { savedRequest ->
            when (savedRequest) {
                is DefaultSavedRequest -> clientIdFromSessionWithinA(savedRequest)
                else -> empty()
        }
    }.or { ofNullable(this.getAttribute("clientId") as String?) }
         .map { ClientAppId(it) }


private fun clientIdFromSessionWithinA(defaultSavedRequest: DefaultSavedRequest): Optional<String> =
     if (defaultSavedRequest.parameterNames.contains("client_id")) {
        ofNullable(defaultSavedRequest.getParameterValues("client_id").firstOrNull())
    } else {
        empty()
    }