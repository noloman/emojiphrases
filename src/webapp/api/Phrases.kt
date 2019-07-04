package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import me.manulorenzo.API_VERSION
import me.manulorenzo.webapp.model.User

const val PHRASES = "$API_VERSION/phrases"

fun Route.phrases(db: Repository) {
    authenticate("auth") {
        get(PHRASES) {
            val user = call.authentication.principal as User
            val phrases: ArrayList<EmojiPhrase> = db.phrases()
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
        post(PHRASES) {
            val params = call.receiveParameters()
            val emojiParam = params["emoji"] ?: throw IllegalArgumentException("Missing required param")
            val phraseParam = params["phrase"] ?: throw IllegalArgumentException("Missing required param")
            db.add(EmojiPhrase(emoji = emojiParam, phrase = phraseParam))
            call.respondRedirect(PHRASES)
        }
    }
}