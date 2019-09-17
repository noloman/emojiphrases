package me.manulorenzo.webapp

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
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
import me.manulorenzo.webapp.model.EPSession
import me.manulorenzo.webapp.model.User
import me.manulorenzo.webapp.repository.Repository

const val SIGNUP = "/signup"
const val MIN_PASSWORD_LENGTH = 6
const val MIN_USER_ID_LENGTH = 4

@KtorExperimentalLocationsAPI
@Location(SIGNUP)
data class Signup(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val error: String = ""
)

@KtorExperimentalLocationsAPI
fun Route.signup(db: Repository, hashFunction: (String) -> String) {
    post<Signup> { signup ->
        val user = call.sessions.get<EPSession>()?.let { epSession -> db.user(epSession.userId) }
        if (user != null) return@post call.redirect(Phrases())

        val signupParams = call.receive<Parameters>()
        val userId = signupParams["userId"] ?: return@post call.redirect(signup)
        val password = signupParams["password"] ?: return@post call.redirect(signup)
        val displayName = signupParams["displayName"] ?: return@post call.redirect(signup)
        val email = signupParams["email"] ?: return@post call.redirect(signup)

        val signUpError = Signup(userId, displayName, email)
        when {
            password.length < MIN_PASSWORD_LENGTH -> call.redirect(signUpError.copy(error = "Password should be at least $MIN_PASSWORD_LENGTH characters long"))
            userId.length < MIN_USER_ID_LENGTH -> call.redirect(signUpError.copy(error = "Username should be at least $MIN_USER_ID_LENGTH characters long"))
            !userNameValid(userId) -> call.redirect(signUpError.copy(error = "Username should consist of digits, letters dots and underscores"))
            db.user(userId) != null -> call.redirect(signUpError.copy(error = "User with the following username is already registered"))
            else -> {
                val hash = hashFunction(password)
                val newUser = User(userId, email, displayName, hash)

                try {
                    db.createUser(newUser)
                } catch (e: Throwable) {
                    when {
                        db.user(userId) != null -> call.redirect(signUpError.copy(error = "User with the username $userId is already registered"))
                        db.userByEmail(email) != null -> call.redirect(signUpError.copy(error = "User with the email $email is already registered"))
                        else -> {
                            application.log.error("Failed to register user", e)
                            call.redirect(signUpError.copy(error = "Failed to register user"))
                        }
                    }
                }
                call.sessions.set(EPSession(newUser.userId))
                call.redirect(Phrases())
            }
        }
    }
    get<Signup> { signup ->
        val user = call.sessions.get<EPSession>()?.let { epSession -> db.user(epSession.userId) }
        if (user != null) {
            call.redirect(Phrases())
        } else {
            call.respond(FreeMarkerContent("signup.ftl", mapOf("error" to signup.error)))
        }
    }
}