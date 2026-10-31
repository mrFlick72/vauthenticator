package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mask.SensitiveDataMaskerResolver
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.ticket.domain.Ticket.Companion.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.slf4j.LoggerFactory

class MfaMethodsEnrollment(
    private val accountRepository: AccountRepository,
    private val ticketCreator: TicketCreator,
    private val ticketRepository: TicketRepository,
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
        sendChallengeCode: Boolean = true
    ): TicketId {
        val account = accountFor(userName)
        val mfaAccountMethod = enrolledDeviceFor(userName, mfaMethod, mfaChannel)
        if (mfaAccountMethod.associated) {
            throw MfaAssociationFailureException(
                MfaAssociationFailureReason.ALREADY_ASSOCIATED,
                "Mfa enrollment refused: the mfa device of this channel is already associated"
            )
        }

        // the latest enrollment replaces the pending ones: each ticket allows only a few codes against the same device secret
        ticketRepository.deleteMfaEnrollmentTicketsFor(userName, mfaAccountMethod.mfaDeviceId)

        if (sendChallengeCode) {
            mfaSender.sendMfaChallengeFor(mfaAccountMethod.userName, mfaAccountMethod.mfaDeviceId)
        }

        return ticketCreator.createTicketFor(
            account,
            clientAppId,
            TicketContext.mfaContextFor(
                mfaDeviceId = mfaAccountMethod.mfaDeviceId.content,
                mfaMethod = mfaMethod,
                mfaChannel = mfaChannel
            )
        )
    }

    // the verification link redeems the account's email device without a code; an already associated one is reused
    fun enrollForEmailVerification(userName: String): TicketId {
        val account = accountFor(userName)
        val mfaAccountMethod = enrolledDeviceFor(userName, MfaMethod.EMAIL_MFA_METHOD, account.email)

        return ticketCreator.createTicketFor(
            account,
            ClientAppId.empty(),
            TicketContext.mfaContextFor(
                mfaDeviceId = mfaAccountMethod.mfaDeviceId.content,
                mfaMethod = MfaMethod.EMAIL_MFA_METHOD,
                mfaChannel = account.email,
                autoAssociation = true,
                purpose = EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
            )
        )
    }

    private fun accountFor(userName: String) =
        accountRepository.accountFor(userName) ?: run {
            logger.warn("account not found")
            throw AccountNotFoundException("account not found")
        }

    private fun enrolledDeviceFor(userName: String, mfaMethod: MfaMethod, mfaChannel: String) =
        mfaAccountMethodsRepository.findBy(userName, mfaMethod, mfaChannel)
            ?: mfaAccountMethodsRepository.save(userName, mfaMethod, mfaChannel, false)
}
