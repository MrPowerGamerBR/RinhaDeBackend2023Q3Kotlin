package com.mrpowergamerbr.rinhadebackend2023q3.routes

import com.mrpowergamerbr.rinhadebackend2023q3.RinhaDeBackend2023Q3
import com.mrpowergamerbr.rinhadebackend2023q3.data.Pessoa
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Pessoas
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Stacks
import com.mrpowergamerbr.rinhadebackend2023q3.utils.RegexUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

class GetSearchPessoasRoute(val m: RinhaDeBackend2023Q3) : BaseRoute("/pessoas") {
    companion object {
        private const val MAX_RESULT_COUNT = 50
    }

    override suspend fun onRequest(call: ApplicationCall) {
        val searchQuery = call.parameters["t"]
        if (searchQuery == null) {
            call.respondText("", status = HttpStatusCode.BadRequest)
            return
        }

        // TODO: To be honest this is a bit hacky
        val escapedRegexQuery = "(?i).*${RegexUtils.escapeRegex(searchQuery)}.*"

        val result = m.database.transaction {
            val users = mutableListOf<Pessoa>()

            // I hate this because it feels extremely hacky and maybe could be optimized
            val usersFromDatabase = Pessoas.select {
                Pessoas.nome.regexp(escapedRegexQuery) or (Pessoas.apelido like escapedRegexQuery)
            }.limit(MAX_RESULT_COUNT)
                .toList()

            if (usersFromDatabase.isNotEmpty()) {
                val matchedUserIds = usersFromDatabase.map { it[Pessoas.id] }

                // Optimization: Load all stacks using a single query to avoid unnecessary roundtrips
                val queriedStacks = Stacks.select { Stacks.person inList matchedUserIds }
                    .toList()

                users.addAll(
                    usersFromDatabase.map {
                        Pessoa(
                            it[Pessoas.id].value,
                            it[Pessoas.nome],
                            it[Pessoas.apelido],
                            it[Pessoas.nascimento],
                            queriedStacks
                                .filter { stack ->
                                    stack[Stacks.person] == it[Pessoas.id]
                                }.map {
                                    it[Stacks.name]
                                }.ifEmpty { null }
                        )
                    }
                )
            }

            val amountToBeSearchedByStack = MAX_RESULT_COUNT - users.size

            if (amountToBeSearchedByStack > 0) {
                // Now we are going to search by stack, if we don't have enough results
                val matchedUserIds = usersFromDatabase.map { it[Pessoas.id] }

                // Select all stacks...
                val matchedStacks = Stacks.slice(Stacks.person).select {
                    Stacks.name.regexp(escapedRegexQuery) and (Stacks.person notInList matchedUserIds)
                }
                    .groupBy(Stacks.person)
                    .limit(amountToBeSearchedByStack)
                    .toList()

                // Now we need to re-query the user data and the stacks all over again
                // Get all user IDs (they are distinct!)
                val distinctUsers = matchedStacks.map { it[Stacks.person].value }
                if (distinctUsers.isNotEmpty()) {
                    // Optimization: Load all users using a single query to avoid unnecessary roundtrips
                    val queriedUsers = Pessoas.select { Pessoas.id inList distinctUsers }
                    val queriedStacks = Stacks.select { Stacks.person inList distinctUsers }
                        .toList()

                    users.addAll(
                        queriedUsers.map {
                            Pessoa(
                                it[Pessoas.id].value,
                                it[Pessoas.nome],
                                it[Pessoas.apelido],
                                it[Pessoas.nascimento],
                                queriedStacks
                                    .filter { stack ->
                                        stack[Stacks.person] == it[Pessoas.id]
                                    }.map {
                                        it[Stacks.name]
                                    }.ifEmpty { null }
                            )
                        }
                    )
                }
            }

            users
        }

        call.respondText(
            Json.encodeToString(result),
            ContentType.Application.Json
        )
    }
}