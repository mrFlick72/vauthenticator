package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.oauth2ClientId(): ClientAppId? =
    getParameter("client_id")?.let(::ClientAppId)
