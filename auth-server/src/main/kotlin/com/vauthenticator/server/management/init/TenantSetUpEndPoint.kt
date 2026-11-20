package com.vauthenticator.server.management.init

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation
import org.springframework.http.ResponseEntity


@Endpoint(id = "tenant-setup")
class TenantSetUpEndPoint(
    private val accountSetUpJob: AccountSetUpJob,
    private val clientApplicationSetUpJob: ClientApplicationSetUpJob,
    private val keySetUpJob: KeySetUpJob,
    private val communicationTemplateSetUpJob: CommunicationTemplateSetUpJob,
    private val defaultPageSetUpJob: DefaultPageSetUpJob
) {

    @WriteOperation
    fun tenantInit(): ResponseEntity<Unit> {
        // documents first: creating the first accounts sends their welcome email, rendered from the template
        communicationTemplateSetUpJob.execute()
        defaultPageSetUpJob.execute()
        accountSetUpJob.execute()
        clientApplicationSetUpJob.execute()
        keySetUpJob.execute()

        return ResponseEntity.noContent().build()
    }

}