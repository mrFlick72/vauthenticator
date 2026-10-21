package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId

/**
 * One step of the login workflow: something the account holder must pass, after proving their password,
 * before a client app accepts the session (see docs/login-workflow.md).
 *
 * A step only says whether it is required; whether it is passed is remembered in the session
 * ([passedLoginWorkflowSteps]) once its page records it through [CompositeLoginWorkflowEngine.stepPassed].
 */
interface LoginWorkflowHandler {

    /**
     * Stable name of the step, stored in the session among the passed steps. Changing it makes every session
     * that already passed the step pass it again.
     */
    fun id(): String

    /**
     * The page the account holder is redirected to while the step is pending.
     */
    fun view(): String

    /**
     * Whether the account holder must pass this step before the client app accepts the session.
     *
     * @param clientAppId the client app asking for the login, or `null` when the login was not started by an
     * authorization request; a step that depends on the app is never required without one.
     */
    fun isRequiredFor(userName: String, clientAppId: ClientAppId?): Boolean
}
