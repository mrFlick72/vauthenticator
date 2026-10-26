package com.vauthenticator.server.login.failedloginlock.adapter.redis

import com.vauthenticator.server.login.failedloginlock.ConsecutiveFailedLogins
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import java.time.Duration

// INCR and PEXPIRE in one atomic step: a count must never be left without its expiry
private val INCREMENT_WITHIN_WINDOW: RedisScript<Long> = RedisScript.of(
    """
    local failures = redis.call('INCR', KEYS[1])
    redis.call('PEXPIRE', KEYS[1], ARGV[1])
    return failures
    """.trimIndent(),
    Long::class.java
)

class RedisConsecutiveFailedLogins(
    private val redisTemplate: RedisTemplate<String, String>
) : ConsecutiveFailedLogins {

    override fun increment(userName: String, window: Duration): Long =
        redisTemplate.execute(INCREMENT_WITHIN_WINDOW, listOf(keyFor(userName)), window.toMillis().toString())

    override fun reset(userName: String) {
        redisTemplate.delete(keyFor(userName))
    }

    private fun keyFor(userName: String) = "consecutive_failed_logins_$userName"
}
