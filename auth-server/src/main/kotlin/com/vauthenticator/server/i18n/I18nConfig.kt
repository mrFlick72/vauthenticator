package com.vauthenticator.server.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import org.springframework.web.servlet.LocaleResolver

@Configuration(proxyBeanMethods = false)
class I18nConfig {

    @Bean
    fun i18nMessageRepository(resourceLoader: ResourceLoader) = I18nMessageRepository(resourceLoader)

    @Bean
    fun i18nMessageInjector(
        localeResolver: LocaleResolver,
        objectMapper: ObjectMapper,
        i18nMessageRepository: I18nMessageRepository,
        currentHttpServletRequestService : CurrentHttpServletRequestService
    ) = I18nMessageInjector(localeResolver, objectMapper, i18nMessageRepository, currentHttpServletRequestService)
}