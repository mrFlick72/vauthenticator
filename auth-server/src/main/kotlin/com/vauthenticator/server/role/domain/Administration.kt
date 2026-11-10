package com.vauthenticator.server.role.domain

/**
 * Only an Administrator can make someone an Administrator, or stop them being one (ADR 0017), and only an
 * Administrator manages an Administrator account (ADR 0018).
 * Who the caller is, and whether it is an Administrator, is up to the adapter.
 *
 * Not a `fun interface`: a lambda would compile to no class, and `AdministrationArchitectureTest` could not see an
 * implementation written as one.
 */
interface Administration {

    fun isAdministrator(): Boolean

    /**
     * @throws AdministrationRequiredException when the caller is not an Administrator
     */
    fun requireAdministrator() {
        if (!isAdministrator()) {
            throw AdministrationRequiredException("only an administrator can do this")
        }
    }
}

class AdministrationRequiredException(message: String) : RuntimeException(message)
