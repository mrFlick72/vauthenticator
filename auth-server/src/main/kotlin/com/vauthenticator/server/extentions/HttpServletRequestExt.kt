package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.oauth2ClientId(): ClientAppId? =
    getParameter("client_id")?.let(::ClientAppId)

/** The client app this request is for (`client_id` parameter, then the saved authorization request), if any. */
fun HttpServletRequest.requestedClientAppId(): ClientAppId? =
    oauth2ClientId() ?: getSession(false)?.oauth2ClientId()

/**
 * The client app a login is for, or no client app ([ClientAppId.empty]) when the login has none (a direct `/login`).
 * Not the System Client: that stands for actions the server starts itself (ADR 0016).
 */
fun HttpServletRequest.loginClientAppId(): ClientAppId =
    requestedClientAppId() ?: ClientAppId.empty()
