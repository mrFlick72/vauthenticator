package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountPattern
import java.time.LocalDateTime

interface PasswordLifeCycleRepository {

    fun store(rule: PasswordLifeCycleRule)

    fun delete(userName: String, action: PasswordLifeCycleAction)

    /**
     * Deletes the rules for [action] whose username matches [pattern] and returns how many were deleted.
     */
    fun deleteMatching(pattern: AccountPattern, action: PasswordLifeCycleAction): Int

    fun updateLastEvaluationDate(rule: PasswordLifeCycleRule, lastEvaluationDate: LocalDateTime)

    fun findRulesFor(userName: String): List<PasswordLifeCycleRule>

    /**
     * Keyset pagination ordered by (userName, action): returns up to [size] rules that come strictly after [after],
     * or the first page when [after] is null.
     */
    fun findAllRulesAfter(after: PasswordLifeCycleRule?, size: Int): List<PasswordLifeCycleRule>

}
