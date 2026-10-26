package com.vauthenticator.server.oauth2.clientapp

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ReadClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.StoreClientApplication
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ClientApplicationConfig {

    @Bean
    fun readClientApplication(clientApplicationRepository: ClientApplicationRepository) =
        ReadClientApplication(clientApplicationRepository)

    @Bean
    fun storeClientApplication(
        clientApplicationRepository: ClientApplicationRepository,
        passwordEncoder: VAuthenticatorPasswordEncoder
    ) =
        StoreClientApplication(clientApplicationRepository, passwordEncoder)

}