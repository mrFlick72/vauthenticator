package com.vauthenticator.server.oidc.logout

import com.vauthenticator.server.extentions.toSha256
import com.vauthenticator.server.oidc.sessionmanagement.OPBS_COOKIE_NAME
import com.vauthenticator.server.oidc.sessionmanagement.OPBS_SESSION_ATTRIBUTE
import com.vauthenticator.server.oidc.sessionmanagement.SessionManagementFactory
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@ExtendWith(MockKExtension::class)
class ClearSessionStateLogoutHandlerTest {

    @MockK
    private lateinit var sessionFactory: SessionManagementFactory

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String?>

    @MockK(relaxed = true)
    private lateinit var hashOperations: HashOperations<String, String, String?>

    @Test
    fun `when logout then opbs state is cleared from session cookie and redis`() {
        every { sessionFactory.sessionIdFor(any()) } returns "session-id"
        every { redisTemplate.opsForHash<String, String?>() } returns hashOperations
        every { hashOperations.get("session-id", "session-id".toSha256()) } returns "session-state"

        val request = MockHttpServletRequest()
        request.setCookies(Cookie("JSESSIONID", "session-id"), Cookie(OPBS_COOKIE_NAME, "opbs-value"))
        val session = request.getSession() ?: throw IllegalStateException("Expected test session")
        session.setAttribute(OPBS_SESSION_ATTRIBUTE, "opbs-value")
        val response = MockHttpServletResponse()

        ClearSessionStateLogoutHandler(sessionFactory, redisTemplate).logout(request, response, null)

        assertNull(session.getAttribute(OPBS_SESSION_ATTRIBUTE))
        val expiredOpbsCookie = response.cookies.single { it.name == OPBS_COOKIE_NAME }
        assertEquals("", expiredOpbsCookie.value)
        assertEquals("/", expiredOpbsCookie.path)
        assertEquals(0, expiredOpbsCookie.maxAge)
        assertEquals(false, expiredOpbsCookie.isHttpOnly)
        verify { hashOperations.delete("session-id", "session-id".toSha256()) }
        verify { hashOperations.delete("session-state", "session-state".toSha256()) }
    }
}
