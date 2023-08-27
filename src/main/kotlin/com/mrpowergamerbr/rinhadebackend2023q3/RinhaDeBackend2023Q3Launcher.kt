package com.mrpowergamerbr.rinhadebackend2023q3

import com.mrpowergamerbr.rinhadebackend2023q3.tables.Pessoas
import com.mrpowergamerbr.rinhadebackend2023q3.tables.Stacks
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils

object RinhaDeBackend2023Q3Launcher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info { "OLÁ E SEJAM BEM-VINDOS A RINHA DE BACKEND 2023 Q3™" }
        logger.info { "*música épica de fundo*" }
        logger.info { "Implementação feita por MrPowerGamerBR" }
        logger.info { "Available Processors: ${Runtime.getRuntime().availableProcessors()}" }

        val database = DatabaseStuff.createPostgreSQLPudding(
            System.getenv("POSTGRESQL_ADDRESS"),
            System.getenv("POSTGRESQL_DATABASE"),
            System.getenv("POSTGRESQL_USERNAME"),
            System.getenv("POSTGRESQL_PASSWORD"),
        )

        runBlocking {
            database.transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    Pessoas,
                    Stacks
                )

                if (System.getenv("POSTGRESQL_TRGM")?.toBoolean() == true) {

                    when (System.getenv("POSTGRESQL_TRGM_INDEX_TYPE")?.uppercase() ?: error("Missing TRGM index type!")) {
                        "GIN" -> {
                            exec("CREATE INDEX IF NOT EXISTS apelido_trgm_idx ON pessoas USING GIN(apelido gin_trgm_ops);")
                            exec("CREATE INDEX IF NOT EXISTS nome_trgm_idx ON pessoas USING GIN(nome gin_trgm_ops);")
                            exec("CREATE INDEX IF NOT EXISTS stack_trgm_idx ON stacks USING GIN(name gin_trgm_ops);")
                        }
                        "GIST" -> {
                            exec("CREATE INDEX IF NOT EXISTS apelido_trgm_idx ON pessoas USING GIST(apelido gist_trgm_ops);")
                            exec("CREATE INDEX IF NOT EXISTS nome_trgm_idx ON pessoas USING GIST(nome gist_trgm_ops);")
                            exec("CREATE INDEX IF NOT EXISTS stack_trgm_idx ON stacks USING GIST(name gist_trgm_ops);")
                        }
                    }
                }
            }
        }

        val m = RinhaDeBackend2023Q3(database)
        m.start()
    }
}