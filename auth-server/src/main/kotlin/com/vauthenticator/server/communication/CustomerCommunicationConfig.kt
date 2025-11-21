package com.vauthenticator.server.communication

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("no-reply.email")
data class NoReplyEMailConfiguration(
    val from: String = "",
    val welcomeEMailSubject: String = "",
    val verificationEMailSubject: String = "",
    val resetPasswordEMailSubject: String = "",
    val mfaEMailSubject: String = ""
)