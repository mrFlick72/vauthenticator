package com.vauthenticator.server.account.adapter

import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.support.AccountTestFixture.anAccount
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

@ExtendWith(MockKExtension::class)
internal class CachedAccountRepositoryTest {

    @MockK
    lateinit var delegate: AccountRepository

    @MockK
    lateinit var cacheOperation: CacheOperation<String, String>

    @MockK
    lateinit var accountCacheContentConverter: AccountCacheContentConverter

    lateinit var underTest: AccountRepository

    @BeforeEach
    fun setUp() {
        underTest = CachedAccountRepository(accountCacheContentConverter, cacheOperation, delegate)
    }


    @Test
    internal fun `when an account is not found into the cache then it is loaded from database and loaded into the cache`() {
        val account = anAccount()
        val username = account.username

        every { cacheOperation.get(account.email) } returns Optional.empty()
        every { delegate.accountFor(username) } returns Optional.of(account)
        every { accountCacheContentConverter.loadableContentIntoCacheFor(account) } returns "account"
        every { cacheOperation.put(username, "account") } just runs

        underTest.accountFor(username)

        verify { cacheOperation.get(account.email) }
        verify { delegate.accountFor(username) }
        verify { accountCacheContentConverter.loadableContentIntoCacheFor(account) }
        verify { cacheOperation.put(username, "account") }
    }

    @Test
    internal fun `when an account is found from the cache `() {
        val account = anAccount()
        val username = account.username

        every { accountCacheContentConverter.getObjectFromCacheContentFor("account") } returns account
        every { cacheOperation.get(account.email) } returns Optional.of("account")

        underTest.accountFor(username)

        verify { cacheOperation.get(account.email) }
        verify(exactly = 0) { delegate.accountFor(username) }
        verify { accountCacheContentConverter.getObjectFromCacheContentFor("account") }
        verify(exactly = 0) { cacheOperation.put(username, "account") }
    }


    @Test
    fun `when an account is updated`() {
        val account = anAccount()
        every { cacheOperation.evict(account.email) } just runs
        every { delegate.save(account) } just runs

        underTest.save(account)

        verify { cacheOperation.evict(account.email) }
        verify { delegate.save(account) }
    }

}