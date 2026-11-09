package com.vauthenticator.server.oauth2.clientapp

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationAdministration
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.DeleteClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.ReadClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.StoreClientApplication
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.Administration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ClientApplicationConfig {

    @Bean
    fun readClientApplication(clientApplicationRepository: ClientApplicationRepository) =
        ReadClientApplication(clientApplicationRepository)

    @Bean
    fun clientApplicationAdministration(administration: Administration) =
        ClientApplicationAdministration(administration)

    @Bean
    fun storeClientApplication(
        clientApplicationRepository: ClientApplicationRepository,
        clientApplicationAdministration: ClientApplicationAdministration,
        passwordEncoder: VAuthenticatorPasswordEncoder
    ) =
        StoreClientApplication(clientApplicationRepository, clientApplicationAdministration, passwordEncoder)

    @Bean
    fun deleteClientApplication(
        clientApplicationRepository: ClientApplicationRepository,
        clientApplicationAdministration: ClientApplicationAdministration
    ) =
        DeleteClientApplication(clientApplicationRepository, clientApplicationAdministration)

}