package com.mrpowergamerbr.rinhadebackend2023q3.routes

import com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3
import com.mrpowergamerbr.rinhadebackend2023q3.utils.HostnameUtils
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.sequins.ktor.BaseRoute

class GetInfoRoute(val m: RinhaDeBackend2023Q3) : BaseRoute("/") {
    override suspend fun onRequest(call: ApplicationCall) {
        call.respondText("Olá RINHA DE BACKEND 2023 Q3™! ${HostnameUtils.getHostname()}")
    }
}