package com.vauthenticator.server.extentions

/**
 * Makes client-controlled text (a typed username, a client id, a user agent, a forwarded IP) safe to write in a log
 * line: a backslash is doubled, so it cannot pass for an escape, and every control character (CR, LF, NEL, ESC, ...),
 * Unicode line or paragraph separator and format character (bidi overrides, zero-width characters) is written as an
 * escape, so the text can neither forge log lines nor disguise itself in a terminal or log viewer.
 */
fun String.escapedForLog(): String =
    buildString {
        this@escapedForLog.forEach {
            when {
                it == '\\' -> append("\\\\")
                it == '\r' -> append("\\r")
                it == '\n' -> append("\\n")
                it.needsLogEscape() -> append("\\u%04x".format(it.code))
                else -> append(it)
            }
        }
    }

private fun Char.needsLogEscape() =
    isISOControl() || this == ' ' || this == ' ' || Character.getType(this) == Character.FORMAT.toInt()
