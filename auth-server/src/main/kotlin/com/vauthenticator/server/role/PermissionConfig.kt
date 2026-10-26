package com.vauthenticator.server.role

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PermissionConfig {

    @Bean
    fun permissionValidator(clientApplicationRepository: ClientApplicationRepository) =
        PermissionValidator(clientApplicationRepository)
}