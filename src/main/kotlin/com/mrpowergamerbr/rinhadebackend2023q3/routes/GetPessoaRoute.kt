package com.mrpowergamerbr.rinhadebackend2023q3.routes

import com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3
import com.mrpowergamerbr.rinhadebackend2023q3.data.Pessoa
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Pessoas
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Stacks
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import java.util.*

class GetPessoaRoute(val m: RinhaDeBackend2023Q3) : BaseRoute("/pessoas/{id}") {
    override suspend fun onRequest(call: ApplicationCall) {
        val userIdAsString = call.parameters.getOrFail("id")
        val userId = try {
            UUID.fromString(userIdAsString)
        } catch (e: IllegalArgumentException) {
            call.respondText("", status = HttpStatusCode.BadRequest)
            return
        }

        val result = m.database.transaction {
            val pessoa = Pessoas.select {
                Pessoas.id eq userId
            }.limit(1)
                .firstOrNull() ?: return@transaction GetPessoaResult.NotFound

            GetPessoaResult.Success(
                pessoa,
                Stacks.select {
                    Stacks.person eq pessoa[Pessoas.id]
                }.toList()
            )
        }

        when (result) {
            GetPessoaResult.NotFound -> {
                call.respondText("", status = HttpStatusCode.NotFound)
                return
            }
            is GetPessoaResult.Success -> {
                call.respondText(
                    Json.encodeToString(
                        Pessoa(
                            userId,
                            result.pessoa[Pessoas.apelido],
                            result.pessoa[Pessoas.nome],
                            result.pessoa[Pessoas.nascimento],
                            result.stack.map {
                                it[Stacks.name]
                            }.ifEmpty { null }
                        )
                    ),
                    ContentType.Application.Json
                )
            }
        }
    }

    sealed class GetPessoaResult {
        class Success(
            val pessoa: ResultRow,
            val stack: List<ResultRow>
        ) : GetPessoaResult()
        object NotFound : GetPessoaResult()
    }
}