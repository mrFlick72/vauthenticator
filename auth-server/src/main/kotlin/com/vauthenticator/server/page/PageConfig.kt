package com.vauthenticator.server.page

import com.vauthenticator.server.page.domain.PageLocation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PageConfig {

    @Bean
    fun pageLocation(@Value("\${asset-server.on-s3.bundle-version:}") bundleVersion: String) =
        PageLocation(bundleVersion)
}
