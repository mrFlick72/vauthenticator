package com.vauthenticator.server.login.workflow

import jakarta.servlet.http.HttpSession

private const val PASSED_LOGIN_WORKFLOW_STEPS = "PASSED_LOGIN_WORKFLOW_STEPS"

fun HttpSession.passedLoginWorkflowSteps(): Set<String> =
    (getAttribute(PASSED_LOGIN_WORKFLOW_STEPS) as? Set<*>)
        ?.filterIsInstance<String>()
        ?.toSet()
        ?: emptySet()

fun HttpSession.loginWorkflowStepPassed(step: LoginWorkflowHandler) =
    setAttribute(PASSED_LOGIN_WORKFLOW_STEPS, HashSet(passedLoginWorkflowSteps() + step.id()))

fun HttpSession.forgetPassedLoginWorkflowSteps() = removeAttribute(PASSED_LOGIN_WORKFLOW_STEPS)
