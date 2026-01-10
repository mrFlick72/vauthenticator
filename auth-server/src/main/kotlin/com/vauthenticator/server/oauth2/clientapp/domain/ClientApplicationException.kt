package com.vauthenticator.server.oauth2.clientapp.domain

class InsufficientClientApplicationScopeException(message: String) : RuntimeException(message)

class UnsupportedClientAppOperationException(message: String) : RuntimeException(message)

class InvalidAppDataException(message: String) : RuntimeException(message)