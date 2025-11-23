package com.vauthenticator.server.oidc.sessionmanagement

import com.vauthenticator.server.extentions.toSha256
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.web.RedirectStrategy
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import java.util.Arrays.stream

fun sendAuthorizationResponse(
    redisTemplate: RedisTemplate<String, String?>,
    factory: SessionManagementFactory,
    redirectStrategy: RedirectStrategy
) = { request: HttpServletRequest,
      response: HttpServletResponse,
      authentication: Authentication ->

    val authorizationCodeRequestAuthentication = authentication as OAuth2AuthorizationCodeRequestAuthenticationToken
    val uriBuilder = UriComponentsBuilder
        .fromUriString(authorizationCodeRequestAuthentication.redirectUri!!)
        .queryParam(OAuth2ParameterNames.CODE, authorizationCodeRequestAuthentication.authorizationCode!!.tokenValue)
    if (StringUtils.hasText(authorizationCodeRequestAuthentication.state)) {
        uriBuilder.queryParam(OAuth2ParameterNames.STATE, authorizationCodeRequestAuthentication.state)
    }

    val sessionState = factory.sessionStateFor(request, authentication)

    val sessionId = factory.sessionIdFor(request)
    redisTemplate.opsForHash<String, String?>().put(sessionId, sessionId.toSha256(), sessionState)
    redisTemplate.opsForHash<String, String?>()
        .put(sessionState, sessionState.toSha256(), factory.opbsStateValue(request))

    uriBuilder.queryParam("session_state", sessionState)
    redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString())
}

class SessionManagementFactory(private val providerSettings: AuthorizationServerSettings) {
    private val logger: Logger = LoggerFactory.getLogger(SessionManagementFactory::class.java)
    fun sessionIdFor(request: HttpServletRequest) =
        stream(request.cookies)
            .filter { it.name.equals("SESSION") }
            .map { it.value }
            .findFirst()
            .orElseThrow()

    fun opbsStateValue(request: HttpServletRequest): String {
        var opbs: String = (request.session.getAttribute("opbs_session_value") ?: "") as String
        if (opbs.isEmpty()) {
            opbs = UUID.randomUUID().toString();
            request.session.setAttribute("opbs_session_value", opbs)
        }

        logger.debug("opbs $opbs")
        return opbs
    }

    fun sessionStateFor(
        request: HttpServletRequest,
        authentication: OAuth2AuthorizationCodeRequestAuthenticationToken
    ): String {
        val userName = authentication.name
        val clientId = authentication.clientId
        val issuer = providerSettings.issuer
        val salt = saltFor(request)

        return "$clientId $issuer ${opbsStateValue(request)} $salt".toSha256() + ".$salt"
    }

    fun saltFor(request: HttpServletRequest) =
        opbsStateValue(request).toSha256()


}

@Controller
class SessionManagementIFrameController(
    @Value("\${consoleDebug:false}") private val consoleDebug: Boolean,
    private val providerSettings: AuthorizationServerSettings
) {

    @GetMapping("/session/management")
    fun sessionManagerIframe(model: Model): String {
        val issuer = providerSettings.issuer

        model.addAttribute("issuer", issuer)
        model.addAttribute("console_debug", consoleDebug)
        return "session/management"
    }

}

@RestController
class CheckSessionEndPoint(private val redisTemplate: RedisTemplate<String, String?>) {

    @GetMapping("/check_session")
    fun checkSession(@RequestParam state: String): ResponseEntity<CheckSessionResponse> = Optional.ofNullable(
        redisTemplate.opsForHash<String, String?>().get(state, state.toSha256())
    )
        .map { ResponseEntity.ok(CheckSessionResponse(it)) }
        .orElseGet { ResponseEntity.notFound().build() }

}


data class CheckSessionResponse(val state: String?)