package com.vauthenticator.server.role.adapter

import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleCacheContentConverter
import com.vauthenticator.server.support.JsonUtils.prettifyInOneLineJsonFrom
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper

private const val testableResource = "roles/rolesSample.json"

private val roleList = listOf(Role("ROLE_1", "ROLE_1"), Role("ROLE_2", "ROLE_2"))
private val rawValue = prettifyInOneLineJsonFrom(testableResource)


class RoleCacheContentConverterTest {

    private val underTest = RoleCacheContentConverter(ObjectMapper())

    @Test
    fun `when a string value from the cache is deserialized`() {
        val actual = underTest.getObjectFromCacheContentFor(rawValue)
        Assertions.assertEquals(roleList, actual)
    }

    @Test
    fun `when a role object is serialized as cached value`() {
        val actual = underTest.loadableContentIntoCacheFor(roleList)
        Assertions.assertEquals(rawValue, actual)
    }
}