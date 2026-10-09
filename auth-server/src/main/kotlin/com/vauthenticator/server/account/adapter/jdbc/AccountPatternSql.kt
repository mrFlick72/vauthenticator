package com.vauthenticator.server.account.adapter.jdbc

import com.vauthenticator.server.account.domain.AccountPattern

/**
 * Translates an [AccountPattern] into a SQL LIKE pattern to be used with ESCAPE '\': the LIKE wildcards `%` and `_`
 * (and the escape character itself) are escaped so they stay literal, then `*` becomes `%`.
 */
fun AccountPattern.toSqlLikePattern(): String =
    value.replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
        .replace("*", "%")
