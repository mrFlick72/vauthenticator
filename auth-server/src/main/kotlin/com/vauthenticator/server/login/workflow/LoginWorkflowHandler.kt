package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId


interface LoginWorkflowHandler {

    fun id(): String

    fun view(): String

    fun isRequiredFor(userName: String, clientAppId: ClientAppId?): Boolean
}
