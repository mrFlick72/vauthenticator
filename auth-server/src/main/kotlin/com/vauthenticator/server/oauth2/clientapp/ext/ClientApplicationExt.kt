package com.vauthenticator.server.oauth2.clientapp.ext

import com.vauthenticator.server.oauth2.clientapp.domain.*

fun ClientApplication.hasEnoughScopes(scopes: Scopes): Boolean {
    return scopes.content.stream().allMatch { this.scopes.content.contains(it) }
        .or(this.scopes.content.contains(Scope.ADMIN_FULL_ACCESS))

}

fun ClientApplication.hasEnoughScopes(scope: Scope) = hasEnoughScopes(Scopes(setOf(scope)))
