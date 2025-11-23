package com.vauthenticator.server.support

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

object RedisUtils {

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