package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import me.manulorenzo.API_VERSION

const val PHRASES = "$API_VERSION/phrases"

fun Route.phrases(db: Repository) {
    get(PHRASES) {
        val phrases: ArrayList<EmojiPhrase> = db.phrases()
        call.respond(phrases.toArray())
    }
}