package com.vauthenticator.management.config

import com.vauthenticator.management.proxy.ApiServiceCallProxyService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ProxyConfig {

    @Bean
    fun apiServiceCallProxyService() =
            ApiServiceCallProxyService("/secure")

}