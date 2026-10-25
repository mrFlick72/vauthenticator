package com.vauthenticator.server.login.failedloginlock.adapter.redis

import com.vauthenticator.server.support.RedisUtils.aRedisTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.util.UUID

class RedisConsecutiveFailedLoginsTest {

    private val window = Duration.ofSeconds(60)

    private lateinit var redisTemplate: RedisTemplate<String, String>
    private lateinit var uut: RedisConsecutiveFailedLogins
    private lateinit var userName: String

    @BeforeEach
    fun setUp() {
        redisTemplate = aRedisTemplate()
        uut = RedisConsecutiveFailedLogins(redisTemplate)
        userName = "${UUID.randomUUID()}@domain.com"
    }

    @Test
    fun `when failed logins are recorded the count grows and lives for the window`() {
        assertEquals(1, uut.increment(userName, window))
        assertEquals(2, uut.increment(userName, window))

        assertEquals(window.seconds, redisTemplate.getExpire("consecutive_failed_logins_$userName"))
    }

    @Test
    fun `when the count is reset it starts again from one`() {
        uut.increment(userName, window)
        uut.increment(userName, window)

        uut.reset(userName)

        assertFalse(redisTemplate.hasKey("consecutive_failed_logins_$userName"))
        assertEquals(1, uut.increment(userName, window))
    }
}
