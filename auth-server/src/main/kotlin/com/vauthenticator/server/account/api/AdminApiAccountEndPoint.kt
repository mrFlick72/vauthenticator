package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.ok
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
class AdminApiAccountEndPoint(
    private val accountRepository: AccountRepository,
    private val accountUpdateAdminAction: AccountUpdateAdminAction,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/admin/accounts/{email}/email")
    fun findAccountFor(authentication: Authentication?, @PathVariable email: String) = run {
        permissionValidator.validate(authentication, Scopes.from(Scope.READ_ACCOUNT))
        ok(
            accountRepository.accountFor(email)
                ?.let { AdminApiAccountApiConverter.fromDomainToAccountAdminApiRepresentation(it) }
        )
    }

    @PutMapping("/api/admin/accounts")
    fun saveAccount(authentication: Authentication?, @RequestBody representation: AdminAccountApiRequest) = run {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.SAVE_ACCOUNT))
        accountUpdateAdminAction.execute(representation, principal.clientAppId())
            .let { noContent().build<Unit>() }
    }

}

data class AdminApiAccountApiRepresentation(
    val accountLocked: Boolean = true,
    val enabled: Boolean = true,
    var email: String = "",
    val authorities: Set<String> = setOf(),
    val mandatoryAction: String = AccountMandatoryAction.NO_ACTION.name,
    val lockedUntil: Instant? = null
)

object AdminApiAccountApiConverter {
    fun fromDomainToAccountAdminApiRepresentation(domain: Account): AdminApiAccountApiRepresentation =
        AdminApiAccountApiRepresentation(
            !domain.accountNonLocked,
            domain.enabled,
            domain.email,
            domain.authorities,
            domain.mandatoryAction.name,
            domain.lockedUntil
        )

}
