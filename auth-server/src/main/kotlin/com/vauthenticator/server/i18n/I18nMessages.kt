package com.vauthenticator.server.i18n

typealias I18nMessageKey = String
typealias I18nMessageValue = String

data class I18nMessages(
    val messages: Map<I18nMessageKey, I18nMessageValue>,
    val error: Map<I18nMessageKey, I18nMessageValue>,
) {
    companion object {
        fun empty() = I18nMessages(emptyMap(), emptyMap())
    }
}

enum class I18nScope(val prefix: String) {
    LOGIN_PAGE("login_page"),

    SIGN_UP_PAGE("sign_up_page"),
    SUCCESSFUL_SIGN_UP_PAGE("successful_sign_up_page"),

    MFA_PAGE("mfa_page"),

    SUCCESSFUL_MAIL_VERIFY_PAGE("successful_mail_verify_page"),

    RESET_PASSWORD_CHALLENGE_SENDER_PAGE("reset_password_challenge_sender_page"),
    SUCCESSFUL_RESET_PASSWORD_CHALLENGE_SENDER_PAGE("successful_reset_password_challenge_sender_page"),

    RESET_PASSWORD_PAGE("reset_password_page"),

    SUCCESSFUL_RESET_PASSWORD_PAGE("successful_reset_password_page"),

    CHANGE_PASSWORD_PAGE("change_password_page"),
}