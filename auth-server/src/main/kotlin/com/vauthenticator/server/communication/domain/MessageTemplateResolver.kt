package com.vauthenticator.server.communication.domain

interface MessageTemplateResolver {

    fun compile(template: String, context: Map<String, Any>): String

}