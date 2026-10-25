package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.oauth2ClientId(): ClientAppId? =
    getParameter("client_id")?.let(::ClientAppId)

/** The client app a login is for, or the system client when the login has none (a direct `/login`). */
fun HttpServletRequest.loginClientAppId(): ClientAppId =
    oauth2ClientId() ?: getSession(false)?.oauth2ClientId() ?: ClientAppId.system()
