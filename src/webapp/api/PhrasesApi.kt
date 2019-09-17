package me.manulorenzo.webapp.api

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import me.manulorenzo.API_VERSION
import me.manulorenzo.webapp.repository.Repository

const val PHRASES_API_ENDPOINT = "$API_VERSION/phrases"

@KtorExperimentalLocationsAPI
@Location(PHRASES_API_ENDPOINT)
class PhrasesApi

@KtorExperimentalLocationsAPI
fun Route.phrasesApi(db: Repository) {
    authenticate("jwt") {
        get<PhrasesApi> {
            call.respond(db.phrases())
        }
    }
}