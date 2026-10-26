package com.vauthenticator.server.cache

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.support.RedisUtils.aRedisTemplate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration
import java.util.*

class RedisCacheOperationTest {
    lateinit var underTest: CacheOperation<String, String>
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private val a_cache = "a_cache"
    private val ttl = Duration.ofSeconds(10)

    @BeforeEach
    fun setUp() {
        redisTemplate = aRedisTemplate()
        underTest = RedisCacheOperation(a_cache, ttl, redisTemplate)
    }


    @Test
    fun `when we are able to put an object in cache`() {
        val key = "a_key"
        val expected = "a_value"
        underTest.put(key, expected)

        val actual = redisTemplate.opsForHash<String, String>().get("a_cache_$key", "a_cache_$key".toSha256())
        val actualExpire = redisTemplate.opsForHash<String, String>().operations.getExpire("a_cache_$key")

        Assertions.assertEquals(expected, actual)
        Assertions.assertEquals(ttl.seconds, actualExpire)
    }

    @Test
    fun `when we are able to get an object in cache`() {
        val key = "a_key"
        val value = "a_value"

        redisTemplate.opsForHash<String, String>().put("a_cache_$key", "a_cache_$key".toSha256(), value)
        val actual = underTest.get(key)

        Assertions.assertEquals(value, actual)
    }

    @Test
    fun `when we are able to evict an object in cache`() {
        val key = "a_key"
        val value = "a_value"

        redisTemplate.opsForHash<String, String>().put("a_cache_$key", "a_cache_$key".toSha256(), value)
        underTest.evict(key)
        val actual = Optional.ofNullable(
            redisTemplate.opsForHash<String, String>().get("a_cache_$key", "a_cache_$key".toSha256())
        )

        Assertions.assertEquals(Optional.empty<String>(), actual)
    }

    @Test
    fun `when all the objects of a cache are evicted the other caches are untouched`() {
        val anotherCache = RedisCacheOperation<String, String>("another_cache", ttl, redisTemplate)
        underTest.put("a_key", "a_value")
        underTest.put("another_key", "another_value")
        anotherCache.put("a_key", "a_value_in_another_cache")

        underTest.evictAll()

        Assertions.assertNull(underTest.get("a_key"))
        Assertions.assertNull(underTest.get("another_key"))
        Assertions.assertEquals("a_value_in_another_cache", anotherCache.get("a_key"))
    }
}
