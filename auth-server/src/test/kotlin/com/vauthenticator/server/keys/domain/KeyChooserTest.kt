package com.vauthenticator.server.keys.domain

import com.vauthenticator.server.support.KeysUtils.aKeyFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KeyChooserTest {
    @Test
    fun `when one key is chosen at random`() {
        val assignedKeys = mutableSetOf<Kid>()
        val firstKey = getKeyWith("FIRST_KID")
        val secondKey = getKeyWith("SECOND_KID")
        val thirdKey = getKeyWith("THIRD_KID")

        val keys = Keys(listOf(firstKey, secondKey, thirdKey))

        keys.peekOneAtRandomWithout(assignedKeys)
        keys.peekOneAtRandomWithout(assignedKeys)
        keys.peekOneAtRandomWithout(assignedKeys)


        assertEquals(3, assignedKeys.size)
    }

    private fun getKeyWith(kid: String) =aKeyFor("A_MASTER_KEY",kid)
}