package com.vauthenticator.server.extentions

import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext


fun JwtEncodingContext.isATokenForAUserFrom(): Boolean =
    !this.authorizationGrantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)