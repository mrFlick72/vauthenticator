package com.vauthenticator.server.password.domain.lifecycle

import java.time.LocalDateTime

interface PasswordLifeCycleRepository {

    fun store(rule: PasswordLifeCycleRule)

    fun delete(userName: String, action: PasswordLifeCycleAction)

    fun updateLastEvaluationDate(rule: PasswordLifeCycleRule, lastEvaluationDate: LocalDateTime)

    fun findRulesFor(userName: String): List<PasswordLifeCycleRule>

    /**
     * Keyset pagination ordered by (userName, action): returns up to [size] rules that come strictly after [after],
     * or the first page when [after] is null.
     */
    fun findAllRulesAfter(after: PasswordLifeCycleRule?, size: Int): List<PasswordLifeCycleRule>

}
