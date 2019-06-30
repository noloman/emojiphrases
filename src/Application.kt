package me.manulorenzo

import io.ktor.application.Application
import io.ktor.routing.routing
import me.manulorenzo.webapp.about
import me.manulorenzo.webapp.home

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    routing {
        home()
        about()
    }
}

