package com.vauthenticator.server.support

import org.springframework.web.context.request.RequestAttributes

object RequestAttributesFixture {

    val requestAttributes = object : RequestAttributes {
        override fun getAttribute(name: String, scope: Int): Any? {
            TODO("Not yet implemented")
        }

        override fun setAttribute(name: String, value: Any, scope: Int) {
            TODO("Not yet implemented")
        }

        override fun removeAttribute(name: String, scope: Int) {
            TODO("Not yet implemented")
        }

        override fun getAttributeNames(scope: Int): Array<String> {
            TODO("Not yet implemented")
        }

        override fun registerDestructionCallback(name: String, callback: Runnable, scope: Int) {
            TODO("Not yet implemented")
        }

        override fun resolveReference(key: String): Any? {
            TODO("Not yet implemented")
        }

        override fun getSessionId() = "SESSION_ID"

        override fun getSessionMutex(): Any {
            TODO("Not yet implemented")
        }

    }
}