package com.vauthenticator.server.oidc.logout

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.oidc.sessionmanagement.OPBS_COOKIE_NAME
import com.vauthenticator.server.oidc.sessionmanagement.OPBS_SESSION_ATTRIBUTE
import com.vauthenticator.server.oidc.sessionmanagement.SessionManagementFactory
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler


class ClearSessionStateLogoutHandler(
    private val sessionFactory: SessionManagementFactory,
    private val redisTemplate: RedisTemplate<String, String?>

) : LogoutHandler {

    override fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        request.getSession(false)?.removeAttribute(OPBS_SESSION_ATTRIBUTE)
        response.addCookie(Cookie(OPBS_COOKIE_NAME, "").apply {
            path = "/"
            maxAge = 0
            isHttpOnly = false
        })

        val sessionId = sessionFactory.sessionIdFor(request)
        val hashOperations = redisTemplate.opsForHash<String, String?>()
        val sessionState = hashOperations.get(sessionId, sessionId.toSha256()) ?: return
        hashOperations.delete(sessionId, sessionId.toSha256())
        hashOperations.delete(sessionState, sessionState.toSha256())
    }

}
