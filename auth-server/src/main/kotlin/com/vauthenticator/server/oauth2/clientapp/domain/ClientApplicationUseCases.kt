package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import java.util.*

class StoreClientApplication(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val passwordEncoder: VAuthenticatorPasswordEncoder
) {
    fun store(aClientApp: ClientApplication, storeWithPassword: Boolean) {
        aClientApp.validate()
        clientApplicationRepository.save(clientApplication(storeWithPassword, aClientApp))
    }

    fun resetPassword(clientAppId: ClientAppId, secret: Secret) {
        clientApplicationRepository.findOne(clientAppId)
            .map {
                if(!it.confidential){
                    throw UnsupportedClientAppOperationException("Reset client application secret for public client s is not supported")
                }
                it.copy(secret = Secret(passwordEncoder.encode(secret.content)))
            }
            .ifPresentOrElse(
                { clientApplicationRepository.save(it) },
                { throw ClientApplicationNotFound("the client application ${clientAppId.content} was not found") }
            )
    }

    private fun clientApplication(storeWithPassword: Boolean, aClientApp: ClientApplication): ClientApplication {
        return if (storeWithPassword) {
            aClientApp.copy(secret = Secret(passwordEncoder.encode(aClientApp.secret.content)))
        } else {
            clientApplicationRepository.findOne(clientAppId = aClientApp.clientAppId)
                .map { app -> aClientApp.copy(secret = app.secret) }
                .orElseThrow()
        }
    }

}

open class ReadClientApplication(private val clientApplicationRepository: ClientApplicationRepository) {
    open fun findOne(clientAppId: ClientAppId): Optional<ClientApplication> =
        clientApplicationRepository.findOne(clientAppId)
            .map { it.copy(secret = Secret("*******")) }

    open fun findAll(): List<ClientApplication> =
        clientApplicationRepository.findAll()
            .map { it.copy(secret = Secret("*******")) }
}