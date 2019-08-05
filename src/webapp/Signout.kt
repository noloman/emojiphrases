package me.manulorenzo.webapp

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.routing.Route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import me.manulorenzo.redirect
import me.manulorenzo.webapp.model.EPSession

const val SIGNOUT = "/signout"

@KtorExperimentalLocationsAPI
@Location(SIGNOUT)
class Signout

@KtorExperimentalLocationsAPI
fun Route.signout() {
    get<Signout> {
        call.sessions.clear<EPSession>()
        call.redirect(Signin())
    }
}