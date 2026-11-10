package com.vauthenticator.server.password.api

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountPattern
import com.vauthenticator.server.account.domain.InvalidAccountPatternException
import com.vauthenticator.server.extentions.clientAppId
import com.vauthenticator.server.extentions.escapedForLog
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.lifecycle.InvalidPasswordLifeCycleIntervalException
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleBulkOutcome
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRules
import com.vauthenticator.server.role.domain.PermissionValidator
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Duration

private const val ACCOUNT_RULES_PATH = "/api/admin/accounts/{userName}/password/lifecycle"
private const val ACCOUNT_RULE_PATH = "$ACCOUNT_RULES_PATH/{action}"
private const val BULK_RULES_PATH = "/api/admin/accounts/password/lifecycle/{action}/bulk"

@RestController
class PasswordLifeCycleEndPoint(
    private val permissionValidator: PermissionValidator,
    private val passwordLifeCycleRules: PasswordLifeCycleRules
) {

    private val logger = LoggerFactory.getLogger(PasswordLifeCycleEndPoint::class.java)

    @PutMapping(ACCOUNT_RULE_PATH)
    fun registerRule(
        @PathVariable userName: String,
        @PathVariable action: PasswordLifeCycleAction,
        @RequestBody request: PasswordLifeCycleRuleRequest,
        authentication: Authentication?
    ): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        passwordLifeCycleRules.register(userName, action, intervalOf(request.intervalSeconds))
        return ResponseEntity.noContent().build()
    }

    @GetMapping(ACCOUNT_RULES_PATH)
    fun rulesFor(
        @PathVariable userName: String,
        authentication: Authentication?
    ): ResponseEntity<List<PasswordLifeCycleRuleRepresentation>> {
        permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        return ResponseEntity.ok(passwordLifeCycleRules.rulesFor(userName).map { it.toRepresentation() })
    }

    @DeleteMapping(ACCOUNT_RULE_PATH)
    fun removeRule(
        @PathVariable userName: String,
        @PathVariable action: PasswordLifeCycleAction,
        authentication: Authentication?
    ): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        passwordLifeCycleRules.remove(userName, action)
        return ResponseEntity.noContent().build()
    }

    @PostMapping(BULK_RULES_PATH)
    fun registerRuleForAccountPattern(
        @PathVariable action: PasswordLifeCycleAction,
        @RequestBody request: PasswordLifeCycleBulkRuleRequest,
        authentication: Authentication?
    ): ResponseEntity<PasswordLifeCycleBulkResult> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        val pattern = accountPatternOf(request.accountPattern)
        val outcome = passwordLifeCycleRules.registerAll(pattern, action, intervalOf(request.intervalSeconds))
        return bulkResponseFor(outcome, "register", action, pattern, principal.clientAppId())
    }

    @DeleteMapping(BULK_RULES_PATH)
    fun removeRuleForAccountPattern(
        @PathVariable action: PasswordLifeCycleAction,
        @RequestParam accountPattern: String,
        authentication: Authentication?
    ): ResponseEntity<PasswordLifeCycleBulkResult> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD_LIFECYCLE))

        val pattern = accountPatternOf(accountPattern)
        val outcome = passwordLifeCycleRules.removeAll(pattern, action)
        return bulkResponseFor(outcome, "remove", action, pattern, principal.clientAppId())
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun accountNotFoundExceptionHandler() = ResponseEntity.notFound().build<Unit>()

    @ExceptionHandler(InvalidPasswordLifeCycleIntervalException::class, InvalidAccountPatternException::class)
    fun invalidRequestExceptionHandler(ex: RuntimeException) =
        ResponseEntity.badRequest().body(ex.message)

    // a partly applied bulk request answers 207 with the usual body: which accounts were skipped, how many, and why
    // stay out of the response and only the count is logged (ADR 0018)
    private fun bulkResponseFor(
        outcome: PasswordLifeCycleBulkOutcome,
        operation: String,
        action: PasswordLifeCycleAction,
        pattern: AccountPattern,
        clientAppId: ClientAppId
    ): ResponseEntity<PasswordLifeCycleBulkResult> {
        val body = PasswordLifeCycleBulkResult(outcome.applied)
        if (outcome.skipped == 0) {
            return ResponseEntity.ok(body)
        }
        logger.warn(
            "bulk {} of the {} password lifecycle rule for pattern {} by client app {} skipped {} accounts only an administrator can manage",
            operation, action, pattern.value.escapedForLog(), clientAppId.content.escapedForLog(), outcome.skipped
        )
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(body)
    }

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
