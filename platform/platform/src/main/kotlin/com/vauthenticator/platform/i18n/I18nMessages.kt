package com.vauthenticator.platform.i18n

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
