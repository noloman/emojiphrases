package me.manulorenzo.webapp.api

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import me.manulorenzo.API_VERSION
import me.manulorenzo.apiUser
import me.manulorenzo.webapp.api.requests.PhrasesApiRequest
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
        post<PhrasesApi> {
            call.apiUser?.let {
                try {
                    val request = call.receive<PhrasesApiRequest>()
                    val phrase = db.add(it.userId, request.emoji, request.phrase)
                    if (phrase != null) {
                        call.respond(phrase)
                    } else {
                        call.respondText("Invalid data received", status = HttpStatusCode.InternalServerError)
                    }
                } catch (e: Throwable) {
                    call.respondText("Invalid data received", status = HttpStatusCode.BadRequest)
                }
            } ?: run {
                call.respondText("Invalid user", status = HttpStatusCode.InternalServerError)
            }
        }
    }
}