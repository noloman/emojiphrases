package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import me.manulorenzo.redirect
import me.manulorenzo.userNameValid
import me.manulorenzo.webapp.api.Phrases
import me.manulorenzo.webapp.model.EPSession
import me.manulorenzo.webapp.repository.Repository

const val SIGNIN = "/signin"

@KtorExperimentalLocationsAPI
@Location(SIGNIN)
data class Signin(val userId: String = "", val error: String = "")

@KtorExperimentalLocationsAPI
fun Route.signin(db: Repository, hashFunction: (String) -> String) {
    post<Signin> { signin ->
        val signinParams = call.receive<Parameters>()
        val userId = signinParams["userId"] ?: return@post call.redirect(signin)
        val password = signinParams["password"] ?: return@post call.redirect(signin)

        val signInError = Signin(userId)

        val signin = when {
            userId.length < MIN_USER_ID_LENGTH -> null
            password.length < MIN_PASSWORD_LENGTH -> null
            !userNameValid(userId) -> null
            else -> db.user(userId, hashFunction(password))
        }
        if (signin == null) {
            call.redirect(signInError.copy(error = "Invalid username or password"))
        } else {
            call.sessions.set(EPSession(userId = signin.userId))
            call.redirect(Phrases())
        }
    }
    get<Signin> { signin ->
        val user = call.sessions.get<EPSession>()?.let { db.user(userId = signin.userId) }
        if (user != null) {
            call.redirect(Home())
        } else {
            call.respond(FreeMarkerContent("signin.ftl", mapOf("userId" to signin.userId, "error" to signin.error)))
        }
    }
}