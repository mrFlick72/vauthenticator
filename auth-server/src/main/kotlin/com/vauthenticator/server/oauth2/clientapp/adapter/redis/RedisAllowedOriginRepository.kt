package com.vauthenticator.server.oauth2.clientapp.adapter.redis

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import org.springframework.data.redis.core.RedisTemplate

class RedisAllowedOriginRepository(val redisTemplate: RedisTemplate<String, String>) :
    AllowedOriginRepository {

    override fun getAllAvailableAllowedOrigins(): Set<AllowedOrigin> {
        val operations = redisTemplate.opsForHash<String, String>()
        return redisTemplate.keys("allowed_origin.*")
            .flatMap { operations.values(it) }
            .flatMap { it.split(",") }
            .filter { it.isNotEmpty() }
            .map { AllowedOrigin(it) }
            .toSet()
    }

    override fun setAllowedOriginsFor(clientAppId: ClientAppId, allowedOrigins: AllowedOrigins) {
        val operations = redisTemplate.opsForHash<String, String>()
        val key = "allowed_origin.${clientAppId.content}"
        val value = allowedOrigins.content.map { it.content }.joinToString(",")
        operations.put(key, key.toSha256(), value)
    }

    override fun deleteAllowedOriginsFor(clientAppId: ClientAppId) {
        val operations = redisTemplate.opsForHash<String, String>()
        val key = "allowed_origin.${clientAppId.content}"
        operations.delete(key, key.toSha256())
    }

}