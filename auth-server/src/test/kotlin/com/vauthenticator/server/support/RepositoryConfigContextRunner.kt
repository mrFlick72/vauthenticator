package com.vauthenticator.server.support

import com.vauthenticator.server.cache.CacheConfig
import io.mockk.mockk
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient
import tools.jackson.databind.ObjectMapper

object RepositoryConfigContextRunner {

    fun aRepositoryConfigContextRunner(vararg configurations: Class<*>): ApplicationContextRunner =
        ApplicationContextRunner()
            .withInitializer { it.beanFactory.conversionService = ApplicationConversionService.getSharedInstance() }
            .withUserConfiguration(CacheConfig::class.java, *configurations)
            .withBean(JdbcTemplate::class.java, { mockk<JdbcTemplate>(relaxed = true) })
            .withBean(NamedParameterJdbcTemplate::class.java, { mockk<NamedParameterJdbcTemplate>(relaxed = true) })
            .withBean(JdbcClient::class.java, { mockk<JdbcClient>(relaxed = true) })
            .withBean(RedisTemplate::class.java, { mockk<RedisTemplate<*, *>>(relaxed = true) })
            .withBean(ObjectMapper::class.java, { ObjectMapper() })

    fun cacheEnabledFor(region: String): Array<String> = arrayOf(
        "vauthenticator.cache.$region.enabled=true",
        "vauthenticator.cache.$region.ttl=1m",
        "vauthenticator.cache.$region.name=${region}_cache"
    )
}
