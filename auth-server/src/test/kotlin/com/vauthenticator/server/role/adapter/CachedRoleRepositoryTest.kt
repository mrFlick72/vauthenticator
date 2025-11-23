package com.vauthenticator.server.role.adapter

import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.adminRole
import com.vauthenticator.server.support.defaultRole
import com.vauthenticator.server.support.roles
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

private const val caceh_content = "roles-junit-content"

private const val cache_key = "roles"


@ExtendWith(MockKExtension::class)
class CachedRoleRepositoryTest {

    @MockK
    lateinit var delegate: RoleRepository

    @MockK
    lateinit var cacheOperation: CacheOperation<String, String>

    @MockK
    lateinit var roleCacheContentConverter: CacheContentConverter<List<Role>>

    lateinit var underTest: RoleRepository

    @BeforeEach
    fun setUp() {
        underTest = CachedRoleRepository(roleCacheContentConverter, cacheOperation, delegate)
    }


    @Test
    internal fun `when a role is not found into the cache then it is loaded from database and loaded into the cache`() {
        every { cacheOperation.get(cache_key) } returns Optional.empty()
        every { delegate.findAll() } returns roles
        every { roleCacheContentConverter.loadableContentIntoCacheFor(roles) } returns caceh_content
        every { cacheOperation.put(cache_key, caceh_content) } just runs

        underTest.findAll()

        verify { cacheOperation.get(cache_key) }
        verify { delegate.findAll() }
        verify { roleCacheContentConverter.loadableContentIntoCacheFor(roles) }
        verify { cacheOperation.put(cache_key, caceh_content) }
    }

    @Test
    internal fun `when an account is found from the cache `() {
        every { cacheOperation.get(cache_key) } returns Optional.of(caceh_content)
        every { roleCacheContentConverter.getObjectFromCacheContentFor(caceh_content) } returns roles

        underTest.findAll()

        verify { cacheOperation.get(cache_key) }
        verify(exactly = 0) { delegate.findAll() }
        verify { roleCacheContentConverter.getObjectFromCacheContentFor(caceh_content) }
        verify(exactly = 0) { cacheOperation.put(cache_key, caceh_content) }
    }


    @Test
    fun `when an account is updated`() {
        every { cacheOperation.evict(cache_key) } just runs
        every { delegate.save(defaultRole) } just runs

        underTest.save(defaultRole)

        verify { cacheOperation.evict(cache_key) }
        verify { delegate.save(defaultRole) }
    }
    @Test
    fun `when an account is deleted`() {
        every { cacheOperation.evict(cache_key) } just runs
        every { delegate.delete(adminRole.name) } just runs

        underTest.delete(adminRole.name)

        verify { cacheOperation.evict(cache_key) }
        verify { delegate.delete(adminRole.name) }
    }
}