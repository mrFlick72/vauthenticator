package com.vauthenticator.server.login.failedloginlock.adapter.redis

import com.vauthenticator.server.login.failedloginlock.ConsecutiveFailedLogins
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

class RedisConsecutiveFailedLogins(
    private val redisTemplate: RedisTemplate<String, String>
) : ConsecutiveFailedLogins {

    override fun increment(userName: String, window: Duration): Long {
        val key = keyFor(userName)
        val failures = redisTemplate.opsForValue().increment(key) ?: 1
        redisTemplate.expire(key, window)
        return failures
    }

    override fun reset(userName: String) {
        redisTemplate.delete(keyFor(userName))
    }

    private fun keyFor(userName: String) = "consecutive_failed_logins_$userName"
}
