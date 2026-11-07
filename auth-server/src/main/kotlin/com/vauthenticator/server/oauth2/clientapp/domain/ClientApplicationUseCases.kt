package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder

class StoreClientApplication(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val clientApplicationAdministration: ClientApplicationAdministration,
    private val passwordEncoder: VAuthenticatorPasswordEncoder
) {
    fun store(aClientApp: ClientApplication, storeWithPassword: Boolean) {
        aClientApp.validate()
        val stored = clientApplicationRepository.findOne(aClientApp.clientAppId)
        val clientApplication = clientApplication(storeWithPassword, aClientApp, stored)
        clientApplicationAdministration.checkSave(stored, clientApplication)
        clientApplicationRepository.save(clientApplication)
    }

    fun resetPassword(clientAppId: ClientAppId, secret: Secret) {
        val stored = clientApplicationRepository.findOne(clientAppId)
            ?: throw ClientApplicationNotFound("the client application ${clientAppId.content} was not found")
        if (!stored.confidential) {
            throw UnsupportedClientAppOperationException("Reset client application secret for public client s is not supported")
        }
        val clientApplication = stored.copy(secret = Secret(passwordEncoder.encode(secret.content)))
        clientApplicationAdministration.checkSave(stored, clientApplication)
        clientApplicationRepository.save(clientApplication)
    }

    private fun clientApplication(
        storeWithPassword: Boolean,
        aClientApp: ClientApplication,
        stored: ClientApplication?
    ): ClientApplication =
        if (storeWithPassword) {
            aClientApp.copy(secret = Secret(passwordEncoder.encode(aClientApp.secret.content)))
        } else {
            stored?.let { app -> aClientApp.copy(secret = app.secret) }
                ?: throw ClientApplicationNotFound("the client application ${aClientApp.clientAppId.content} was not found")
        }

}

class DeleteClientApplication(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val clientApplicationAdministration: ClientApplicationAdministration
) {
    fun delete(clientAppId: ClientAppId) {
        clientApplicationAdministration.checkDelete(clientApplicationRepository.findOne(clientAppId))
        clientApplicationRepository.delete(clientAppId)
    }
}

open class ReadClientApplication(private val clientApplicationRepository: ClientApplicationRepository) {
    open fun findOne(clientAppId: ClientAppId): ClientApplication? =
        clientApplicationRepository.findOne(clientAppId)?.copy(secret = Secret("*******"))

    open fun findAll(): List<ClientApplication> =
        clientApplicationRepository.findAll()
            .map { it.copy(secret = Secret("*******")) }
}