package com.vauthenticator.server.oauth2.clientapp.adapter.redis

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.oauth2.clientapp.adapter.AbstractAllowedOriginRepositoryTest
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.support.RedisUtils

class RedisAllowedOriginRepositoryTest : AbstractAllowedOriginRepositoryTest() {
    val redisTemplate = RedisUtils.aRedisTemplate()


    override fun resetDatabase() {
        val opsForHash = redisTemplate.opsForHash<String, String>()

        redisTemplate.keys("allowed_origin.*")
            .forEach { opsForHash.delete(it, it.toSha256())}


        opsForHash.put(
            "allowed_origin.ONE_CLIENT_APP_ID",
            "allowed_origin.ONE_CLIENT_APP_ID".toSha256(),
            "http://localhost:8080"
        )
        opsForHash.put(
            "allowed_origin.ANOTHER_CLIENT_APP_ID",
            "allowed_origin.ANOTHER_CLIENT_APP_ID".toSha256(),
            "http://localhost:9090"
        )

    }

    override fun initUnitUnderTest(): AllowedOriginRepository {
        return RedisAllowedOriginRepository(redisTemplate)
    }


}