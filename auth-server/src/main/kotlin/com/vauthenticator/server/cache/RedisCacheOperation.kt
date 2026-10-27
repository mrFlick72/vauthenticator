package com.vauthenticator.server.cache

import com.vauthenticator.server.extentions.toSha256
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import java.time.Duration

class RedisCacheOperation<K, O>(
    private val cacheName: String,
    private val ttl: Duration,
    private val redisTemplate: RedisTemplate<String, O>
) : CacheOperation<K, O> {

    private val logger = LoggerFactory.getLogger(RedisCacheOperation::class.java)

    override fun get(key: K): O? {
        val keyAsString = getKeyAsStringFor(key)
        val valueFromRedis = redisTemplate.opsForHash<String, O>().get(keyAsString, keyAsString.toSha256())
        logger.debug("item get from cache for key: {}", key)

        return valueFromRedis
    }

    override fun put(key: K, value: O) {
        val keyAsString = getKeyAsStringFor(key)
        redisTemplate.opsForHash<String, O>().put(keyAsString, keyAsString.toSha256(), value)
        redisTemplate.opsForHash<String, O>().operations.expire(keyAsString, ttl)
        logger.debug("item put in cache for key: {} with ttl: {}", key, ttl)
    }


    override fun evict(key: K) {
        val keyAsString = getKeyAsStringFor(key)
        redisTemplate.opsForHash<String, O>().delete(keyAsString, keyAsString.toSha256())
        logger.debug("cache evicted for {}", key)
    }

    override fun evictAll() {
        val keys = redisTemplate.scan(ScanOptions.scanOptions().match("${cacheName}_*").build())
            .use { it.asSequence().toList() }
        if (keys.isNotEmpty()) {
            redisTemplate.unlink(keys)
        }
        logger.debug("cache {} evicted", cacheName)
    }

    private fun getKeyAsStringFor(key: K) = "${cacheName}_${key.toString()}"
}
