package com.vauthenticator.server.management.init

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TenantSetUpEndPointTest {

    @RelaxedMockK
    lateinit var accountSetUpJob: AccountSetUpJob

    @RelaxedMockK
    lateinit var clientApplicationSetUpJob: ClientApplicationSetUpJob

    @RelaxedMockK
    lateinit var keySetUpJob: KeySetUpJob

    @RelaxedMockK
    lateinit var communicationTemplateSetUpJob: CommunicationTemplateSetUpJob

    @RelaxedMockK
    lateinit var defaultPageSetUpJob: DefaultPageSetUpJob

    @Test
    fun `tenant setup saves the default templates before the first accounts, whose welcome email needs them`() {
        val uut = TenantSetUpEndPoint(
            accountSetUpJob,
            clientApplicationSetUpJob,
            keySetUpJob,
            communicationTemplateSetUpJob,
            defaultPageSetUpJob
        )

        uut.tenantInit()

        verifyOrder {
            communicationTemplateSetUpJob.execute()
            accountSetUpJob.execute()
        }
    }
}
