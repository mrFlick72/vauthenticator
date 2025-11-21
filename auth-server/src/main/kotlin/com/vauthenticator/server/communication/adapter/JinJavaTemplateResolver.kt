package com.vauthenticator.server.communication.adapter

import com.hubspot.jinjava.Jinjava
import com.vauthenticator.server.communication.domain.MessageTemplateResolver

class JinJavaTemplateResolver(private val engine: Jinjava) : MessageTemplateResolver {
    override fun compile(template: String, context: Map<String, Any>): String {
        return engine.render(template, context)
    }

}
