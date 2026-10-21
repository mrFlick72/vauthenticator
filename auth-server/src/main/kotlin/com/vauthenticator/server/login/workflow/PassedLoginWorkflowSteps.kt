package com.vauthenticator.server.login.workflow

import jakarta.servlet.http.HttpSession

/*
 * The passed steps of the login workflow, kept in the session as a set of step ids. A passed step stays passed
 * until the session ends or a new password login forgets them; plain strings in a java.util set keep the
 * attribute serializable by Spring Session.
 */
private const val PASSED_LOGIN_WORKFLOW_STEPS = "PASSED_LOGIN_WORKFLOW_STEPS"

/**
 * The [LoginWorkflowHandler.id]s of the steps passed in this session.
 */
fun HttpSession.passedLoginWorkflowSteps(): Set<String> =
    (getAttribute(PASSED_LOGIN_WORKFLOW_STEPS) as? Set<*>)
        ?.filterIsInstance<String>()
        ?.toSet()
        ?: emptySet()

/**
 * Remembers that the step was passed in this session.
 */
fun HttpSession.loginWorkflowStepPassed(step: LoginWorkflowHandler) =
    setAttribute(PASSED_LOGIN_WORKFLOW_STEPS, HashSet(passedLoginWorkflowSteps() + step.id()))

/**
 * Forgets every passed step, so the workflow starts fresh; called on each new password login.
 */
fun HttpSession.forgetPassedLoginWorkflowSteps() = removeAttribute(PASSED_LOGIN_WORKFLOW_STEPS)
