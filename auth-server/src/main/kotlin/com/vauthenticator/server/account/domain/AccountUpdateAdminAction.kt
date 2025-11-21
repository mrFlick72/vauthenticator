package com.vauthenticator.server.account.domain


data class AdminAccountApiRequest(
    val accountLocked: Boolean = true,
    val enabled: Boolean = true,
    var email: String = "",
    val authorities: Set<String> = emptySet(),
    val mandatoryAction: AccountMandatoryAction = AccountMandatoryAction.NO_ACTION
)


class AccountUpdateAdminAction(private val accountRepository: AccountRepository) {
    fun execute(
        request: AdminAccountApiRequest
    ) {
        accountRepository.accountFor(request.email)
            .ifPresent { account ->
                accountRepository.save(
                    account.copy(
                        accountNonLocked = !request.accountLocked,
                        enabled = request.enabled,
                        authorities = request.authorities,
                        mandatoryAction = request.mandatoryAction
                    )
                )
            }
    }

}