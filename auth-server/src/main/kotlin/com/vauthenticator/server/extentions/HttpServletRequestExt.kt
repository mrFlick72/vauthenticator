package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import java.util.*


fun HttpServletRequest.oauth2ClientId(): Optional<ClientAppId> {
    return Optional.ofNullable(this.getParameter("client_id")).map { ClientAppId(it) }

}