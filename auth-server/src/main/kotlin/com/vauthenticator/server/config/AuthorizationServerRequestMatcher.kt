package com.vauthenticator.server.config

import org.springframework.http.HttpMethod
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

/**
 * The requests the authorization server security chain handles: its endpoints, plus the CORS preflight of the
 * userinfo endpoint. Spring Authorization Server matches userinfo for GET and POST only, so without it a
 * browser's OPTIONS /userinfo reaches no chain that applies CORS and is answered without any
 * Access-Control-Allow-* header, which blocks a bearer call to userinfo from another origin.
 */
fun authorizationServerRequestMatcher(endpointsMatcher: RequestMatcher, userInfoEndpoint: String): RequestMatcher =
    OrRequestMatcher(
        endpointsMatcher,
        PathPatternRequestMatcher.pathPattern(HttpMethod.OPTIONS, userInfoEndpoint)
    )
