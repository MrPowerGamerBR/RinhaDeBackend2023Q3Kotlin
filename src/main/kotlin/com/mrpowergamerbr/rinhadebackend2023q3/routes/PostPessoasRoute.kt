package com.mrpowergamerbr.rinhadebackend2023q3.routes

import com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3
import com.mrpowergamerbr.rinhadebackend2023q3.data.CreatePessoaRequest
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Pessoas
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Stacks
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.postgresql.util.PSQLException
import java.util.*

class PostPessoasRoute(val m: RinhaDeBackend2023Q3) : BaseRoute("/pessoas") {
    override suspend fun onRequest(call: ApplicationCall) {
        val bodyAsString = call.receiveText()
        try {
            val createPessoaRequest = Json.decodeFromString<CreatePessoaRequest>(bodyAsString)

            val result = m.database.transaction {
                val id = try {
                    Pessoas.insertAndGetId {
                        it[Pessoas.apelido] = createPessoaRequest.apelido
                        it[Pessoas.nome] = createPessoaRequest.nome
                        it[Pessoas.nascimento] = createPessoaRequest.nascimento
                    }
                } catch (e: ExposedSQLException) {
                    val cause = e.cause
                    if (cause is PSQLException) {
                        if (cause.serverErrorMessage?.message?.contains("duplicate key value violates unique constraint") == true)
                        return@transaction CreatePessoaResult.NicknameAlreadyExists
                    }
                    throw e
                }

                val stackList = createPessoaRequest.stack
                if (stackList != null) {
                    Stacks.batchInsert(stackList, shouldReturnGeneratedValues = false) {
                        this[Stacks.name] = it
                        this[Stacks.person] = id
                    }
                }

                return@transaction CreatePessoaResult.Success(id.value)
            }

            when (result) {
                CreatePessoaResult.NicknameAlreadyExists -> {
                    call.respondText("", status = HttpStatusCode.UnprocessableEntity)
                }
                is CreatePessoaResult.Success -> {
                    call.response.header("Location", "/pessoas/${result.id}")
                    call.respondText("", status = HttpStatusCode.Created)
                }
            }
        } catch (e: SerializationException) {
            // TODO: Missing Bad Request "syntatically incorrect" JSON checks, I don't how we can check for those because kotlinx.serialization sort of does that for us
            call.respondText("", status = HttpStatusCode.UnprocessableEntity)
        } catch (e: IllegalArgumentException) {
            // TODO: Missing Bad Request "syntatically incorrect" JSON checks, I don't how we can check for those because kotlinx.serialization sort of does that for us
            call.respondText("", status = HttpStatusCode.UnprocessableEntity)
        }
    }

    sealed class CreatePessoaResult {
        class Success(val id: UUID) : CreatePessoaResult()
        object NicknameAlreadyExists : CreatePessoaResult()
    }
}