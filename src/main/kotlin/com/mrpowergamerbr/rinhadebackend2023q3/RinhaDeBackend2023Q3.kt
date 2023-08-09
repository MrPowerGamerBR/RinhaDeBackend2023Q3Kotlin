package com.mrpowergamerbr.rinhadebackend2023q3

import com.mrpowergamerbr.rinhadebackend2023q3.routes.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

class RinhaDeBackend2023Q3(val database: DatabaseStuff) {
    val routes = listOf(
        GetInfoRoute(this),
        GetPessoaRoute(this),
        GetSearchPessoasRoute(this),
        PostPessoasRoute(this),
        GetContagemPessoasRoute(this),
    )

    fun start() {
        val server = embeddedServer(CIO, System.getenv("WEBSERVER_PORT").toIntOrNull() ?: 9999) {
            routing {
                for (route in routes) {
                    route.register(this)
                }
            }
        }
        server.start(true)
    }
}