package com.vauthenticator.server.events

import com.vauthenticator.server.extentions.toSha256
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Where an event came from: the request it was created in. The session is referenced by the SHA-256 of its id,
 * never by the id itself, because a session id is a credential and events end up in logs (ADR 0016).
 */
data class RequestContext(
    val ipAddress: String,
    val userAgent: String?,
    val sessionReference: String?
) {
    companion object {
        /** The context of the request bound to the current thread, or null when there is none (e.g. a scheduled job). */
        fun current(): RequestContext? {
            val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
                ?: return null
            return RequestContext(
                ipAddress = request.remoteAddr,
                userAgent = request.getHeader("User-Agent"),
                sessionReference = request.getSession(false)?.id?.toSha256()
            )
        }
    }
}
