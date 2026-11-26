package com.vauthenticator.platform.i18n

import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import java.util.*

/**
 * Reads the messages of one scope from the `i18n/messages_<lang>.properties` bundle on the classpath, falling back to
 * `i18n/messages.properties`: the keys starting with `<scope>.` are the messages, those starting with
 * `errors_<scope>.` the errors. The scopes and the bundles belong to the server.
 */
class I18nMessageRepository(private val resourceLoader: ResourceLoader) {

    private val logger = LoggerFactory.getLogger(I18nMessageRepository::class.java)

    fun getMessagedFor(scope: String, userLang: String): I18nMessages {
        try {
            val properties = Properties()
            val resource = messageResourceFor(userLang)
            properties.load(resource.inputStream)
            val messages = getContentFor(properties, scope, "")
            val errors = getContentFor(properties, scope, "errors_")
            return I18nMessages(messages, errors)
        } catch (e: Exception) {
            logger.error(e.message, e)
            return I18nMessages.empty()
        }
    }

    private fun getContentFor(
        properties: Properties,
        scope: String,
        prefix: String
    ) = (properties.toMap() as Map<String, String>)
        .filter { it.key.startsWith("$prefix$scope") }
        .mapKeys { it.key.removePrefix("$prefix$scope.") }

    private fun messageResourceFor(
        userLang: String
    ): Resource {
        var resource =
            resourceLoader.getResource("${ResourceLoader.CLASSPATH_URL_PREFIX}/i18n/messages_${userLang}.properties")
        if (!resource.exists()) {
            resource = resourceLoader.getResource("${ResourceLoader.CLASSPATH_URL_PREFIX}/i18n/messages.properties")
        }
        return resource
    }

}