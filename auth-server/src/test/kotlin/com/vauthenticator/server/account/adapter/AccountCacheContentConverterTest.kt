package com.vauthenticator.server.account.adapter

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.account.domain.Date
import com.vauthenticator.server.account.domain.Phone
import com.vauthenticator.server.account.domain.UserLocale
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JsonUtils.prettifyInOneLineJsonFrom
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AccountCacheContentConverterTest {
    val underTest = AccountCacheContentConverter(ObjectMapper())

    @Test
    fun `when a complete account from cache is loaded`() {
        val aCompleteAccountCacheContent = prettifyInOneLineJsonFrom(resourceTestFrom("aCompleteAccount.json"))
        val actual = underTest.getObjectFromCacheContentFor(aCompleteAccountCacheContent)
        val expected = aCompleteAccount()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `when a partial account from cache is loaded`() {
        val cacheContent = prettifyInOneLineJsonFrom(resourceTestFrom("aPartialAccount.json"))
        val actual = underTest.getObjectFromCacheContentFor(cacheContent)
        val expected = aPartialAccount()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `when a complete account is made ready for the cache`() {
        val actual = underTest.loadableContentIntoCacheFor(aCompleteAccount())
        val expected = prettifyInOneLineJsonFrom(resourceTestFrom("aCompleteAccount.json"))

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `when a partial account  is made ready for the cache`() {
        val actual = underTest.loadableContentIntoCacheFor(aPartialAccount())
        val expected = prettifyInOneLineJsonFrom(resourceTestFrom("aPartialAccount.json"))

        Assertions.assertEquals(expected, actual)
    }

    private fun aCompleteAccount() = aPartialAccount()
        .copy(
            birthDate = Date.isoDateFor("2023-01-01"),
            phone = Phone.phoneFor("+23 333 2323233"),
            locale = UserLocale.localeFrom("en-US")
        )

    private fun aPartialAccount() = anAccount()

    private fun resourceTestFrom(fileName: String) = "accounts/$fileName"

}