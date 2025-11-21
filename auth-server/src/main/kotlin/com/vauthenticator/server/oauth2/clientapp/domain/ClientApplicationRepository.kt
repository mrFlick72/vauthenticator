package com.vauthenticator.server.oauth2.clientapp.domain

import java.util.*

interface ClientApplicationRepository {

    fun findOne(clientAppId: ClientAppId): Optional<ClientApplication>

    fun findAll(): Iterable<ClientApplication>

    fun save(clientApp: ClientApplication)

    fun delete(clientAppId: ClientAppId)
}

class ClientApplicationNotFound(message: String) : RuntimeException(message)