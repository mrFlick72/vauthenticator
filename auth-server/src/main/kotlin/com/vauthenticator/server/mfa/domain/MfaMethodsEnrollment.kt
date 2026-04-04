package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mask.SensitiveDataMaskerResolver
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketId
import org.slf4j.LoggerFactory

class MfaMethodsEnrollment(
    private val accountRepository: AccountRepository,
    private val ticketCreator: TicketCreator,
    private val mfaSender: MfaChallengeSender,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val sensitiveDataMaskerResolver: SensitiveDataMaskerResolver
) {

    private val logger = LoggerFactory.getLogger(MfaMethodsEnrollment::class.java)

    fun getEnrollmentsFor(userName: String, withMaskedSensibleInformation: Boolean = false): List<MfaDevice> =
        mfaAccountMethodsRepository.getDefaultDevice(userName)?.let { defaultMfaDevice ->
            mfaAccountMethodsRepository.findAll(userName)
                .map {
                    val sensitiveDataMasker = sensitiveDataMaskerResolver.getSensitiveDataMasker(it.mfaMethod)
                    MfaDevice(
                        if (withMaskedSensibleInformation) sensitiveDataMasker.mask(it.userName) else it.userName,
                        it.mfaMethod,
                        if (withMaskedSensibleInformation) sensitiveDataMasker.mask(it.mfaChannel) else it.mfaChannel,
                        it.mfaDeviceId,
                        it.mfaDeviceId.content == defaultMfaDevice.content
                    )
                }
        } ?: emptyList()


    fun enroll(
        userName: String,
        mfaMethod: MfaMethod,
        mfaChannel: String,
        clientAppId: ClientAppId,
        sendChallengeCode: Boolean = true,
        ticketContextAdditionalProperties: Map<String, String> = emptyMap()
    ): TicketId {
        val account = accountRepository.accountFor(userName) ?: run {
                logger.warn("account not found")
                throw AccountNotFoundException("account not found")
            }
        val mfaAccountMethod = mfaAccountMethodsRepository.findBy(userName, mfaMethod, mfaChannel)
            ?: mfaAccountMethodsRepository.save(userName, mfaMethod, mfaChannel, false)

        if (sendChallengeCode) {
            mfaSender.sendMfaChallengeFor(mfaAccountMethod.userName, mfaAccountMethod.mfaDeviceId)
        }

        return ticketCreator.createTicketFor(
            account,
            clientAppId,
            TicketContext.mfaContextFor(
                mfaDeviceId = mfaAccountMethod.mfaDeviceId.content,
                mfaMethod = mfaMethod,
                mfaChannel = mfaChannel,
                ticketContextAdditionalProperties = ticketContextAdditionalProperties
            )
        )
    }
}
