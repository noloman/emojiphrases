package me.manulorenzo.webapp.api

import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.util.KtorExperimentalAPI
import me.manulorenzo.JwtService
import me.manulorenzo.hash
import me.manulorenzo.redirect
import me.manulorenzo.webapp.repository.Repository

const val LOGIN_ENDPOINT = "/login"

@KtorExperimentalLocationsAPI
@Location(LOGIN_ENDPOINT)
class Login

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
fun Route.login(db: Repository, jwt: JwtService) {
    post<Login> {
        val params = call.receive<Parameters>()
        val userId = params["userId"] ?: return@post call.redirect(it)
        val password = params["password"] ?: return@post call.redirect(it)

        val user = db.user(userId, hash(password))
        if (user != null) {
            val token = jwt.generateToken(user)
            call.respondText(token)
        } else {
            call.respondText("Invalid user")
        }
    }
}
