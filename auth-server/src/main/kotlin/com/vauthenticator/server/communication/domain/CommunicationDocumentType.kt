package com.vauthenticator.server.communication.domain

/** The Document Types of the Email Templates and SMS Templates, the storage folder each kind is kept in. */
enum class CommunicationDocumentType(val content: String) {
    EMAIL("email"), SMS("sms")
}
