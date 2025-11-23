package com.vauthenticator.server.password.adapter

import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val A_USERNAME = "A_USERNAME"

abstract class AbstractPasswordHistoryRepositoryTest {

    lateinit var uut: PasswordHistoryRepository

    abstract fun initPasswordHistoryRepository(): PasswordHistoryRepository
    abstract fun resetDatabase()
    abstract fun loadActualDynamoSizeFor(userName: String): List<Map<String, Any>>

    @BeforeEach
    fun setUp() {
        uut = initPasswordHistoryRepository()
        resetDatabase()
    }

    @Test
    fun `when store a new password in an empty the history`() {
        uut.store(A_USERNAME, Password("A_PASSWORD"))
        val history = uut.load(A_USERNAME)

        Assertions.assertEquals(1, history.size)
    }

    @Test
    fun `when store a new password in an non empty the history`() {
        uut.store(A_USERNAME, Password("A_PASSWORD 3"))
        uut.store(A_USERNAME, Password("A_PASSWORD 1"))
        uut.store(A_USERNAME, Password("A_PASSWORD 2"))
        uut.store(A_USERNAME, Password("A_PASSWORD 10"))
        uut.store(A_USERNAME, Password("A_PASSWORD 12"))
        uut.store(A_USERNAME, Password("A_PASSWORD 22"))
        uut.store(A_USERNAME, Password("A_PASSWORD 32"))
        uut.store(A_USERNAME, Password("A_PASSWORD 42"))
        val history = uut.load(A_USERNAME)

        val expected = listOf(
            Password("A_PASSWORD 42"),
            Password("A_PASSWORD 32"),
        )
        Assertions.assertEquals(expected, history)
        Assertions.assertEquals(3, loadActualDynamoSizeFor(A_USERNAME).size)
    }

    @Test
    fun `when store a new password in an non empty the history with less item then requested as limit`() {
        uut.store(A_USERNAME, Password("A_PASSWORD 3"))
        val history = uut.load(A_USERNAME)

        val expected = listOf(
            Password("A_PASSWORD 3"),
        )
        Assertions.assertEquals(expected, history)
        Assertions.assertEquals(1, loadActualDynamoSizeFor(A_USERNAME).size)
    }

}