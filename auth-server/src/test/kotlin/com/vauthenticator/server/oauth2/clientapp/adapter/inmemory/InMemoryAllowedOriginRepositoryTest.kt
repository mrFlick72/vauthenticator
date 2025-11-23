package com.vauthenticator.server.oauth2.clientapp.adapter.inmemory

import com.vauthenticator.server.oauth2.clientapp.adapter.AbstractAllowedOriginRepositoryTest
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId

class InMemoryAllowedOriginRepositoryTest : AbstractAllowedOriginRepositoryTest() {


    override fun resetDatabase() {
    }

    override fun initUnitUnderTest(): AllowedOriginRepository {
        return InMemoryAllowedOriginRepository(
            mutableMapOf(
                ClientAppId("ONE_CLIENT_APP_ID") to AllowedOrigins(
                    setOf(AllowedOrigin("http://localhost:8080"))
                ), ClientAppId("ANOTHER_CLIENT_APP_ID") to AllowedOrigins(
                    setOf(AllowedOrigin("http://localhost:9090"))
                )
            )
        )
    }

}