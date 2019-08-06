package me.manulorenzo.webapp.api

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import me.manulorenzo.API_VERSION
import me.manulorenzo.webapp.model.Request
import me.manulorenzo.webapp.repository.Repository

const val PHRASE_ENDPOINT = "$API_VERSION/phrase"

@KtorExperimentalLocationsAPI
@Location(PHRASE_ENDPOINT)
class Phrase

@KtorExperimentalLocationsAPI
fun Route.phrase(db: Repository) {
    post<Phrase> {
        val request = call.receive<Request>()
        val phrase = db.add("", request.emoji, request.phrase)
        call.respond(phrase)
    }
}