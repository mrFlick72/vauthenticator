package com.vauthenticator.server.password.api

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountPattern
import com.vauthenticator.server.account.domain.InvalidAccountPatternException
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.lifecycle.InvalidPasswordLifeCycleIntervalException
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRules
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.time.Duration

private const val ACCOUNT_RULES_PATH = "/api/admin/accounts/{userName}/password/lifecycle"
private const val ACCOUNT_RULE_PATH = "$ACCOUNT_RULES_PATH/{action}"
private const val BULK_RULES_PATH = "/api/admin/accounts/password/lifecycle/{action}/bulk"

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

        passwordLifeCycleRules.register(userName, action, intervalOf(request.intervalSeconds))
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

    @PostMapping(BULK_RULES_PATH)
    fun registerRuleForAccountPattern(
        @PathVariable action: PasswordLifeCycleAction,
        @RequestBody request: PasswordLifeCycleBulkRuleRequest,
        principal: JwtAuthenticationToken
    ): ResponseEntity<PasswordLifeCycleBulkResult> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        val matchedAccounts = passwordLifeCycleRules.registerAll(
            accountPatternOf(request.accountPattern),
            action,
            intervalOf(request.intervalSeconds)
        )
        return ResponseEntity.ok(PasswordLifeCycleBulkResult(matchedAccounts))
    }

    @DeleteMapping(BULK_RULES_PATH)
    fun removeRuleForAccountPattern(
        @PathVariable action: PasswordLifeCycleAction,
        @RequestParam accountPattern: String,
        principal: JwtAuthenticationToken
    ): ResponseEntity<PasswordLifeCycleBulkResult> {
        permissionValidator.validate(principal, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        val matchedAccounts = passwordLifeCycleRules.removeAll(accountPatternOf(accountPattern), action)
        return ResponseEntity.ok(PasswordLifeCycleBulkResult(matchedAccounts))
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun accountNotFoundExceptionHandler() = ResponseEntity.notFound().build<Unit>()

    @ExceptionHandler(InvalidPasswordLifeCycleIntervalException::class, InvalidAccountPatternException::class)
    fun invalidRequestExceptionHandler(ex: RuntimeException) =
        ResponseEntity.badRequest().body(ex.message)

    private fun intervalOf(intervalSeconds: Long?): Duration =
        Duration.ofSeconds(
            intervalSeconds ?: throw InvalidPasswordLifeCycleIntervalException("The interval in seconds is mandatory")
        )

    private fun accountPatternOf(accountPattern: String?): AccountPattern =
        AccountPattern(accountPattern ?: throw InvalidAccountPatternException("The account pattern is mandatory"))
}

data class PasswordLifeCycleRuleRequest(val intervalSeconds: Long?)

data class PasswordLifeCycleBulkRuleRequest(val accountPattern: String?, val intervalSeconds: Long?)

data class PasswordLifeCycleBulkResult(val matchedAccounts: Int)

data class PasswordLifeCycleRuleRepresentation(
    val action: String,
    val intervalSeconds: Long,
    val creationDate: String,
    val lastEvaluationDate: String?,
    val nextEvaluationDate: String
)

private fun PasswordLifeCycleRule.toRepresentation() = PasswordLifeCycleRuleRepresentation(
    action = action.name,
    intervalSeconds = interval.seconds,
    creationDate = creationDate.toString(),
    lastEvaluationDate = lastEvaluationDate?.toString(),
    nextEvaluationDate = nextEvaluationDate().toString()
)
