package com.vauthenticator.platform.cache

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

// a template on the Redis the tests expect on localhost:6379 (the same convention as the auth-server tests)
object RedisTestTemplates {

    fun aRedisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()

        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = StringRedisSerializer()

        val lettuceConnectionFactory = LettuceConnectionFactory()
        lettuceConnectionFactory.afterPropertiesSet()

        redisTemplate.connectionFactory = lettuceConnectionFactory
        redisTemplate.afterPropertiesSet()
        return redisTemplate
    }
}