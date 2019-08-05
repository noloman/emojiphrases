package me.manulorenzo

import com.ryanharter.ktor.moshi.moshi
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.request.header
import io.ktor.request.host
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.routing
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.util.KtorExperimentalAPI
import me.manulorenzo.webapp.about
import me.manulorenzo.webapp.api.phrase
import me.manulorenzo.webapp.home
import me.manulorenzo.webapp.model.EPSession
import me.manulorenzo.webapp.model.User
import me.manulorenzo.webapp.phrases
import me.manulorenzo.webapp.repository.DatabaseFactory
import me.manulorenzo.webapp.repository.EmojiphrasesRepository
import me.manulorenzo.webapp.signin
import me.manulorenzo.webapp.signout
import me.manulorenzo.webapp.signup
import java.net.URI
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(DefaultHeaders)
    install(StatusPages) {
        exception<Throwable> { e ->
            call.respondText(
                e.localizedMessage,
                ContentType.Text.Plain,
                HttpStatusCode.InternalServerError
            )
        }
    }
    install(ContentNegotiation) {
        moshi()
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Locations)
    install(Sessions) {
        cookie<EPSession>("SESSION") {
            transform(SessionTransportTransformerMessageAuthentication(hashKey))
        }
    }

    val hashFunction = { s: String -> hash(s) }

    DatabaseFactory.init()

    val db = EmojiphrasesRepository()

    routing {
        static("/static") {
            resources("images")
        }
        home(db)
        about(db)
        phrases(db, hashFunction)
        signin(db, hashFunction)
        signout()
        signup(db, hashFunction)
        // API
        phrase(db)
    }
}

const val API_VERSION = "/api/v1"

@KtorExperimentalLocationsAPI
suspend fun ApplicationCall.redirect(location: Any) {
    respondRedirect(application.locations.href(location))
}

fun ApplicationCall.refererHost() =
    request.header(HttpHeaders.Referrer)?.let { referrer: String -> URI.create(referrer).host }

fun ApplicationCall.securityCode(date: Long, user: User, hashFunction: (String) -> String) =
    hashFunction("$date:${user.userId}:${request.host()}:${refererHost()}")

fun ApplicationCall.verifyCode(date: Long, user: User, code: String, hashFunction: (String) -> String) =
    securityCode(date, user, hashFunction) == code &&
            (System.currentTimeMillis() - date).let {
                it > 0 && it < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS)
            }