package com.mrpowergamerbr.rinhadebackend2023q3

import com.mrpowergamerbr.rinhadebackend2023q3.routes.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.tomcat.*

class RinhaDeBackend2023Q3(val database: DatabaseStuff) {
    val routes = listOf(
        GetInfoRoute(this),
        GetPessoaRoute(this),
        GetSearchPessoasRoute(this),
        PostPessoasRoute(this),
        GetContagemPessoasRoute(this),
    )

    fun start() {
        val engine = when (System.getenv("WEBSERVER_ENGINE")?.uppercase() ?: "CIO") {
            "CIO" -> CIO
            "JETTY" -> Jetty
            "NETTY" -> Netty
            "TOMCAT" -> Tomcat
            else -> {
                error("Unknown engine!")
            }
        }

        val server = embeddedServer(engine, System.getenv("WEBSERVER_PORT").toIntOrNull() ?: 9999, configure = {
            connectionGroupSize = (16 / 2 + 1)
            workerGroupSize = (16 / 2 + 1)
            callGroupSize = 16
        }) {
            routing {
                for (route in routes) {
                    route.register(this)
                }
            }
        }
        server.start(true)
    }
}