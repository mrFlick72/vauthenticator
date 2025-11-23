package com.vauthenticator.management.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class BaseUiModelInjector(
    @Value("\${vauthenticator.host}") private val vauthenticatorHost: String,
    @Value("\${vauthenticator.session-management.rp-iframe.host}") private val managementUiHost: String,
    @Value("\${assetServer.baseUrl:http://local.management.vauthenticator.com:\${server.port}/static/content/asset}") private val assetServerBaseUrl: String
) {

    @ModelAttribute("assetServerBaseUrl")
    fun assetServerBaseUrl() = assetServerBaseUrl

    @ModelAttribute("rpSessionManagementIFrame")
    fun rpSessionManagementIFrame() = "$managementUiHost/session/management"

    @ModelAttribute("opSessionManagementIFrame")
    fun opSessionManagementIFrame() = "$vauthenticatorHost/session/management"

}