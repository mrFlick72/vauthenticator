package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder

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
            ?.let {
                if (!it.confidential) {
                    throw UnsupportedClientAppOperationException("Reset client application secret for public client s is not supported")
                }
                it.copy(secret = Secret(passwordEncoder.encode(secret.content)))
            }
            ?.let { clientApplicationRepository.save(it) }
            ?: throw ClientApplicationNotFound("the client application ${clientAppId.content} was not found")
    }

    private fun clientApplication(storeWithPassword: Boolean, aClientApp: ClientApplication): ClientApplication {
        return if (storeWithPassword) {
            aClientApp.copy(secret = Secret(passwordEncoder.encode(aClientApp.secret.content)))
        } else {
            clientApplicationRepository.findOne(clientAppId = aClientApp.clientAppId)
                ?.let { app -> aClientApp.copy(secret = app.secret) }
                ?: throw ClientApplicationNotFound("the client application ${aClientApp.clientAppId.content} was not found")
        }
    }

}

open class ReadClientApplication(private val clientApplicationRepository: ClientApplicationRepository) {
    open fun findOne(clientAppId: ClientAppId): ClientApplication? =
        clientApplicationRepository.findOne(clientAppId)?.copy(secret = Secret("*******"))

    open fun findAll(): List<ClientApplication> =
        clientApplicationRepository.findAll()
            .map { it.copy(secret = Secret("*******")) }
}