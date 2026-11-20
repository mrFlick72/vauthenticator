package com.vauthenticator.server.page.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path

class PageTest {

    @Test
    fun `every webpack entry is a page and every page is a webpack entry`() {
        val webpackConfig = Path.of("src/main/frontend/webpack.config.js").toFile().readText()
        val entries = webpackConfig.substringAfter("entry: {").substringBefore("}")
        val bundles = Regex("""^\s*"?(\w+)"?\s*:""", RegexOption.MULTILINE)
            .findAll(entries)
            .map { "${it.groupValues[1]}_bundle.js" }
            .toSet()

        assertThat(bundles).containsExactlyInAnyOrderElementsOf(Page.entries.map { it.bundle })
    }

    @Test
    fun `a page is stored under content asset`() {
        assertThat(PageLocation("").pathOf("login_bundle.js")).isEqualTo("content/asset/login_bundle.js")
    }

    @Test
    fun `a page is stored under its bundle version when one is configured`() {
        assertThat(PageLocation("0.10").pathOf("login_bundle.js")).isEqualTo("content/asset/0.10/login_bundle.js")
    }
}
