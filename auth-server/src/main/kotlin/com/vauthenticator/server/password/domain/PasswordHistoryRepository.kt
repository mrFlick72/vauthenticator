package com.vauthenticator.server.password.domain

interface PasswordHistoryRepository {

    fun store(userName: String, password: Password)
    fun load(userName: String): List<Password>

}

