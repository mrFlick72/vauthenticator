package com.vauthenticator.server.role.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.cache.CacheContentConverter

data class Role(val name: String, val description: String) {
    companion object {
        fun defaultRole() = Role("ROLE_USER", "ROLE_USER")
    }
}

class ProtectedRoleFromDeletionException(message: String) : RuntimeException(message)

class RoleCacheContentConverter(private val objectMapper: ObjectMapper) : CacheContentConverter<List<Role>> {
    override fun getObjectFromCacheContentFor(cacheContent: String): List<Role> {
        return objectMapper.readValue(cacheContent, object : TypeReference<List<Role>>() {})
    }


    override fun loadableContentIntoCacheFor(source: List<Role>): String =
        objectMapper.writeValueAsString(source)

}
