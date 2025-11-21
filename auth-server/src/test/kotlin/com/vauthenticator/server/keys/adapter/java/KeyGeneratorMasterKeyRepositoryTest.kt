package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.keys.domain.MasterKid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyGeneratorMasterKeyRepositoryTest {

    lateinit var uut: KeyGeneratorMasterKeyRepository

    @BeforeEach
    fun setUp() {
        uut = KeyGeneratorMasterKeyRepository(KeyGeneratorMasterKeyStorage(mapOf("a_key" to "a_value")))
    }


    @Test
    fun `when a key is retrieved`() {
        val expected = "a_value"
        val actual = uut.maskerKeyFor(MasterKid("a_key"))
        assertEquals(expected, actual)
    }


    @Test
    fun `when get a key from the storage fails`() {
        assertThrows(NullPointerException::class.java) { uut.maskerKeyFor(MasterKid("a_key_2")) }
    }
}