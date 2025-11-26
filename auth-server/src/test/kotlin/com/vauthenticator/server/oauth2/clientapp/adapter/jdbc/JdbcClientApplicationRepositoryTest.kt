package com.vauthenticator.server.oauth2.clientapp.adapter.jdbc

import com.vauthenticator.server.oauth2.clientapp.adapter.AbstractClientApplicationRepositoryTest
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.JdbcUtils.namedJdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import tools.jackson.databind.ObjectMapper

class JdbcClientApplicationRepositoryTest : AbstractClientApplicationRepositoryTest() {

    override fun resetDatabase() {
        resetDb()
    }

    override fun initUnitUnderTest(allowedOriginRepository: AllowedOriginRepository): ClientApplicationRepository =
        JdbcClientApplicationRepository(namedJdbcTemplate, ObjectMapper(), allowedOriginRepository)

}