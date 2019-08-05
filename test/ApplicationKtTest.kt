import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import me.manulorenzo.module
import me.manulorenzo.webapp.ABOUT
import me.manulorenzo.webapp.HOME
import org.junit.Test
import kotlin.test.assertEquals

class ApplicationKtTest {
    @Test
    fun `should return an OK when redirected to the root URL`() = withTestApplication({ module() }) {
        with(handleRequest(HttpMethod.Get, HOME)) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `should return an OK when redirected to the about URL`() = withTestApplication({ module() }) {
        with(handleRequest(HttpMethod.Get, ABOUT)) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("About", response.content)
        }
    }
}