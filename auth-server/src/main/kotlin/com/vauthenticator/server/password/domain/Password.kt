package com.vauthenticator.server.password.domain

import com.vauthenticator.platform.events.SensitivePayload

/** A password, even encoded, is a [SensitivePayload]: an event carrying it never writes it to the log. */
@JvmInline
value class Password(val content: String) : SensitivePayload
