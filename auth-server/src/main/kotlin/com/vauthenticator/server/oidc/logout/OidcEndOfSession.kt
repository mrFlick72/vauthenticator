package com.vauthenticator.server.oidc.logout

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.oidc.sessionmanagement.SessionManagementFactory
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Optional.ofNullable

@Controller
class FrontChannelLogoutController(@Value("\${auth.oidcIss:}") private val authServerBaseUrl: String) {

    @GetMapping("/oidc/logout")
    fun frontChannelGlobalLogout(
        model: Model,
        @RequestParam("post_logout_redirect_uri") postLogoutRedirectUri: String,
        @RequestParam("id_token_hint") idTokenHint: String
    ): String {

        model.addAttribute("federatedServers", mutableListOf("$authServerBaseUrl/logout"))
        return "logout/oidc/global_logout"
    }

}


class ClearSessionStateLogoutHandler(
    private val sessionFactory: SessionManagementFactory,
    private val redisTemplate: RedisTemplate<String, String?>

) : LogoutHandler {

    override fun logout(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val sessionId = sessionFactory.sessionIdFor(request)
        val hashOperations = redisTemplate.opsForHash<String, String?>()
        ofNullable(hashOperations.get(sessionId, sessionId.toSha256()))
            .ifPresent {
                hashOperations.delete(sessionId, sessionId.toSha256())
                hashOperations.delete(it, it.toSha256())
            }
    }

}