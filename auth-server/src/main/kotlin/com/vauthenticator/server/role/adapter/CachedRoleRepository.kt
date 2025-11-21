package com.vauthenticator.server.role.adapter

import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository

private const val ROLES_CACHE_KEY = "roles"

class CachedRoleRepository(
    private val cacheContentConverter: CacheContentConverter<List<Role>>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: RoleRepository
) : RoleRepository by delegate {

    override fun findAll(): List<Role> {
        return cacheOperation.get(ROLES_CACHE_KEY)
            .map { cacheContentConverter.getObjectFromCacheContentFor(it) }
            .orElseGet {
                val loadedRoles = delegate.findAll()
                cacheOperation.put(
                    ROLES_CACHE_KEY,
                    cacheContentConverter.loadableContentIntoCacheFor(loadedRoles)
                )
                loadedRoles
            }
    }

    override fun save(role: Role) {
        cacheOperation.evict(ROLES_CACHE_KEY)
        delegate.save(role)
    }

    override fun delete(role: String) {
        cacheOperation.evict(ROLES_CACHE_KEY)
        delegate.delete(role)
    }
}