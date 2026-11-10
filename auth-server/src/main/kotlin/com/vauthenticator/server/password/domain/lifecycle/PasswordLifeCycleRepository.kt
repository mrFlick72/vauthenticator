package com.vauthenticator.server.password.domain.lifecycle

import java.time.LocalDateTime

interface PasswordLifeCycleRepository {

    fun store(rule: PasswordLifeCycleRule)

    /**
     * Deletes the rule of [userName] for [action] and returns how many rules were deleted: 1, or 0 when there was none.
     */
    fun delete(userName: String, action: PasswordLifeCycleAction): Int

    fun updateLastEvaluationDate(rule: PasswordLifeCycleRule, lastEvaluationDate: LocalDateTime)

    fun findRulesFor(userName: String): List<PasswordLifeCycleRule>

    /**
     * Keyset pagination ordered by (userName, action): returns up to [size] rules that come strictly after [after],
     * or the first page when [after] is null.
     */
    fun findAllRulesAfter(after: PasswordLifeCycleRule?, size: Int): List<PasswordLifeCycleRule>

}
