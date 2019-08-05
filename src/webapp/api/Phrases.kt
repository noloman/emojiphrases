package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route
import me.manulorenzo.API_VERSION
import me.manulorenzo.redirect
import me.manulorenzo.webapp.model.User
import me.manulorenzo.webapp.repository.Repository

const val PHRASES = "$API_VERSION/phrases"

@Location(PHRASES)
class Phrases

fun Route.phrases(db: Repository) {
    authenticate("auth") {
        get<Phrases> {
            val user = call.authentication.principal as User
            val phrases: List<EmojiPhrase> = db.phrases()
            call.respond(
                FreeMarkerContent(
                    "phrases.ftl",
                    mapOf(
                        "apiVersion" to API_VERSION,
                        "phrases" to phrases,
                        "displayName" to user.displayName
                    )
                )
            )
        }
        post<Phrases> {
            val params = call.receiveParameters()
            val action = params["action"] ?: throw IllegalArgumentException("Missing required param")
            when (action) {
                "delete" -> {
                    val id = params["id"] ?: throw IllegalArgumentException("Missing required param")
                    db.remove(id)
                }
                "add" -> {
                    val emoji = params["emoji"] ?: throw IllegalArgumentException("Missing required param")
                    val phrase = params["phrase"] ?: throw IllegalArgumentException("Missing required param")
                    db.add(emoji, phrase)
                }
            }
            call.redirect(Phrases())
        }
    }
}