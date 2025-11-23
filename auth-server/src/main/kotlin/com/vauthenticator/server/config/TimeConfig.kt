package com.vauthenticator.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration(proxyBeanMethods = false)
class TimeConfig {

    @Bean
    fun clock(): Clock = Clock.systemUTC()
}