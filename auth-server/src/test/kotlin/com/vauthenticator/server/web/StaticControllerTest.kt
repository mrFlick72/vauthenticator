package com.vauthenticator.server.web

import com.vauthenticator.server.document.domain.Document
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class StaticControllerTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var staticContentLocalCache: CaffeineCache

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(StaticController("",staticContentLocalCache)).build()
    }

    @Test
    fun `when the content is cached`() {
        every { staticContentLocalCache.get("asset.js", Document::class.java) } returns Document(
            "",
            "",
            ByteArray(0)
        )

        mokMvc.perform(
            get("/static/content/asset/asset.js")
        ).andExpect(content().bytes(ByteArray(0)))
    }
}