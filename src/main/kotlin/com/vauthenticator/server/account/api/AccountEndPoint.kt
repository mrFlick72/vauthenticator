package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.AccountMandatoryAction.NO_ACTION
import com.vauthenticator.server.account.api.SignUpAccountConverter.fromRepresentationToSignedUpAccount
import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.account.domain.Date
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.Optional.ofNullable

@RestController
@SessionAttributes("clientId")
class AccountEndPoint(
    private val permissionValidator: PermissionValidator,
    private val signUpUse: SignUpUse,
    private val saveAccount: SaveAccount
) {
    val logger: Logger = LoggerFactory.getLogger(AccountEndPoint::class.java)

    @PostMapping("/api/accounts")
    fun signup(
        session: HttpSession,
        principal: JwtAuthenticationToken?,
        @RequestBody representation: AccountRepresentation
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, session, Scopes.from(Scope.SIGN_UP))
        val account = fromRepresentationToSignedUpAccount(representation)
        clientAppIdFrom(principal, session)
            .map { signUpUse.execute(it, account) }
            .orElseThrow()
        return status(HttpStatus.CREATED).build()
    }

    private fun clientAppIdFrom(
        principal: JwtAuthenticationToken?,
        session: HttpSession
    ): Optional<ClientAppId> =
        ofNullable(principal).map { it.clientAppId() }.or { session.oauth2ClientId() }

    @PutMapping("/api/accounts")
    fun save(
        principal: JwtAuthenticationToken,
        @RequestBody representation: AccountRepresentation
    ): ResponseEntity<Unit> {
        logWarningForNotEmptyUserNameInRequestBodyFor(representation)
        val incompleteAccount = fromRepresentationToSignedUpAccount(representation)
        return saveAccount.execute(principal, incompleteAccount)
            .let { ResponseEntity.noContent().build() }
    }

    private fun logWarningForNotEmptyUserNameInRequestBodyFor(representation: AccountRepresentation) {
        if (representation.email.isNotEmpty()) {
            logger.warn("there is an email in the body.............. it will be ignored in favour of the access token identity")
        }
    }

}


data class AccountRepresentation(
    var email: String = "",
    var password: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var birthDate: String = "",
    var phone: String = "",
    var locale: String = "",
    val authorities: List<String> = emptyList(),
    val groups: List<String> = emptyList()
)

object SignUpAccountConverter {
    fun fromRepresentationToSignedUpAccount(representation: AccountRepresentation): Account =
        Account(
            accountNonExpired = true,
            accountNonLocked = false,
            credentialsNonExpired = true,
            enabled = false,
            emailVerified = false,
            username = representation.email,
            password = representation.password,
            firstName = representation.firstName,
            lastName = representation.lastName,
            email = representation.email,
            authorities = representation.authorities.toSet(),
            groups = representation.groups.toSet(),
            birthDate = Date.isoDateFor(representation.birthDate),
            phone = Phone.phoneFor(representation.phone),
            locale = UserLocale.localeFrom(representation.locale),
            mandatoryAction = NO_ACTION
        )
}