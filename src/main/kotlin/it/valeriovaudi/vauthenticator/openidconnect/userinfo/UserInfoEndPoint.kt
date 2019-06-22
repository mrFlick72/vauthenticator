package it.valeriovaudi.vauthenticator.openidconnect.userinfo

import org.springframework.http.ResponseEntity.ok
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserInfoEndPoint {

    @GetMapping("/user-info")
    fun key(principal: JwtAuthenticationToken) =
            ok(UserInfo(principal.token.claims["user_name"] as String))

}