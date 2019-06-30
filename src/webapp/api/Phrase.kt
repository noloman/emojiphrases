package me.manulorenzo.webapp.api

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import me.manulorenzo.API_VERSION
import me.manulorenzo.webapp.EmojiPhrase
import me.manulorenzo.webapp.Repository
import me.manulorenzo.webapp.model.Request

const val PHRASE_ENDPOINT = "$API_VERSION/phrase"

fun Route.phrase(db: Repository) {
    post(PHRASE_ENDPOINT) {
        val request = call.receive<Request>()
        val phrase = db.add(EmojiPhrase(request.emoji, request.phrase))
        call.respond(phrase)
    }
}