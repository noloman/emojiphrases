package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import me.manulorenzo.API_VERSION
import me.manulorenzo.redirect
import me.manulorenzo.securityCode
import me.manulorenzo.verifyCode
import me.manulorenzo.webapp.model.EPSession
import me.manulorenzo.webapp.model.EmojiPhrase
import me.manulorenzo.webapp.repository.Repository

const val PHRASES = "/phrases"

@KtorExperimentalLocationsAPI
@Location(PHRASES)
class Phrases

@KtorExperimentalLocationsAPI
fun Route.phrases(db: Repository, hashFunction: (String) -> String) {
    get<Phrases> {
        val user = call.sessions.get<EPSession>()?.let { epSession -> db.user(epSession.userId) }
        if (user == null) {
            call.redirect(Signin())
        } else {
            val phrases: List<EmojiPhrase> = db.phrases()
            val date = System.currentTimeMillis()
            val code = call.securityCode(date, user, hashFunction)
            call.respond(
                FreeMarkerContent(
                    "phrases.ftl",
                    mapOf(
                        "apiVersion" to API_VERSION,
                        "phrases" to phrases,
                        "user" to user,
                        "date" to date,
                        "code" to code
                    )
                )
            )
        }
    }
    post<Phrases> { phrases ->
        val user = call.sessions.get<EPSession>()?.let { epSession -> db.user(epSession.userId) }

        val params = call.receiveParameters()
        val date = params["date"]?.toLongOrNull() ?: return@post call.redirect(phrases)
        val code = params["code"] ?: return@post call.redirect(phrases)

        if (user == null || !call.verifyCode(date, user, code, hashFunction)) call.redirect(Signin())

        when (params["action"] ?: throw IllegalArgumentException("Missing required param")) {
            "delete" -> {
                val id = params["id"] ?: throw IllegalArgumentException("Missing required param")
                db.remove(id)
            }
            "add" -> {
                val emoji = params["emoji"] ?: throw IllegalArgumentException("Missing required param")
                val phrase = params["phrase"] ?: throw IllegalArgumentException("Missing required param")
                db.add("", emoji, phrase)
            }
        }
        call.redirect(Phrases())
    }
}