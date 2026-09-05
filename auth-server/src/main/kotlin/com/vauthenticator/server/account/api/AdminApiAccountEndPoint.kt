package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class AdminApiAccountEndPoint(
    private val accountRepository: AccountRepository,
    private val accountUpdateAdminAction: AccountUpdateAdminAction,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/admin/accounts/{email}/email")
    fun findAccountFor(principal: JwtAuthenticationToken, @PathVariable email: String) = run {
        permissionValidator.validate(principal, Scopes.from(Scope.READ_ACCOUNT))
        ok(
            accountRepository.accountFor(email)
                ?.let { AdminApiAccountApiConverter.fromDomainToAccountAdminApiRepresentation(it) }
        )
    }

    @PutMapping("/api/admin/accounts")
    fun saveAccount(principal: JwtAuthenticationToken, @RequestBody representation: AdminAccountApiRequest) = run {
        permissionValidator.validate(principal, Scopes.from(Scope.SAVE_ACCOUNT))
        accountUpdateAdminAction.execute(representation)
            .let { noContent().build<Unit>() }
    }

}

data class AdminApiAccountApiRepresentation(
    val accountLocked: Boolean = true,
    val enabled: Boolean = true,
    var email: String = "",
    val authorities: Set<String> = setOf(),
    val mandatoryAction: String = AccountMandatoryAction.NO_ACTION.name
)

object AdminApiAccountApiConverter {
    fun fromDomainToAccountAdminApiRepresentation(domain: Account): AdminApiAccountApiRepresentation =
        AdminApiAccountApiRepresentation(
            !domain.accountNonLocked,
            domain.enabled,
            domain.email,
            domain.authorities,
            domain.mandatoryAction.name
        )

}
