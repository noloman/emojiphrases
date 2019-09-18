package me.manulorenzo

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import me.manulorenzo.webapp.ABOUT
import me.manulorenzo.webapp.HOME
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiophrasesApplicationTest {
    @Test
    fun `should return an OK when redirected to the root URL`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, HOME)) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `should return an OK when redirected to the about URL`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, ABOUT)) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }
}