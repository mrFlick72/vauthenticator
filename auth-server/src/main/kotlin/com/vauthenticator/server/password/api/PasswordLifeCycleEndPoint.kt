package com.vauthenticator.server.password.api

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.lifecycle.InvalidPasswordLifeCycleIntervalException
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleInterval
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRules
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

private const val ACCOUNT_RULES_PATH = "/api/admin/accounts/{userName}/password/lifecycle"
private const val ACCOUNT_RULE_PATH = "$ACCOUNT_RULES_PATH/{action}"

@Profile("database")
@RestController
class PasswordLifeCycleEndPoint(
    private val permissionValidator: PermissionValidator,
    private val passwordLifeCycleRules: PasswordLifeCycleRules
) {

    @PutMapping(ACCOUNT_RULE_PATH)
    fun registerRule(
        @PathVariable userName: String,
        @PathVariable action: PasswordLifeCycleAction,
        @RequestBody request: PasswordLifeCycleRuleRequest,
        principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        passwordLifeCycleRules.register(userName, action, parseInterval(request.interval))
        return ResponseEntity.noContent().build()
    }

    @GetMapping(ACCOUNT_RULES_PATH)
    fun rulesFor(
        @PathVariable userName: String,
        principal: JwtAuthenticationToken
    ): ResponseEntity<List<PasswordLifeCycleRuleRepresentation>> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        return ResponseEntity.ok(passwordLifeCycleRules.rulesFor(userName).map { it.toRepresentation() })
    }

    @DeleteMapping(ACCOUNT_RULE_PATH)
    fun removeRule(
        @PathVariable userName: String,
        @PathVariable action: PasswordLifeCycleAction,
        principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        passwordLifeCycleRules.remove(userName, action)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun accountNotFoundExceptionHandler() = ResponseEntity.notFound().build<Unit>()

    @ExceptionHandler(InvalidPasswordLifeCycleIntervalException::class)
    fun invalidIntervalExceptionHandler(ex: InvalidPasswordLifeCycleIntervalException) =
        ResponseEntity.badRequest().body(ex.message)

    private fun parseInterval(interval: String?): PasswordLifeCycleInterval =
        PasswordLifeCycleInterval.parse(
            interval ?: throw InvalidPasswordLifeCycleIntervalException("The interval is mandatory")
        )
}

data class PasswordLifeCycleRuleRequest(val interval: String?)

data class PasswordLifeCycleRuleRepresentation(
    val action: String,
    val interval: String,
    val creationDate: String,
    val lastEvaluationDate: String?,
    val nextEvaluationDate: String
)

private fun PasswordLifeCycleRule.toRepresentation() = PasswordLifeCycleRuleRepresentation(
    action = action.name,
    interval = interval.toString(),
    creationDate = creationDate.toString(),
    lastEvaluationDate = lastEvaluationDate?.toString(),
    nextEvaluationDate = nextEvaluationDate().toString()
)
