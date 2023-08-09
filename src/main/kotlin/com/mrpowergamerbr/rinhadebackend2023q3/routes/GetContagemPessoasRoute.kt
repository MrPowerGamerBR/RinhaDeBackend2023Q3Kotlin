package com.mrpowergamerbr.rinhadebackend2023q3.routes

import com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Pessoas
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.selectAll

class GetContagemPessoasRoute(val m: RinhaDeBackend2023Q3) : BaseRoute("/contagem-pessoas") {
    override suspend fun onRequest(call: ApplicationCall) {
        val count = m.database.transaction {
            Pessoas.selectAll().count()
        }

        call.respondText(count.toString())
    }
}