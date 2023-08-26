package com.mrpowergamerbr.rinhadebackend2023q3

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Transaction
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DatabaseStuff(
    val hikariDataSource: HikariDataSource,
    val database: Database,
    private val cachedThreadPool: ExecutorService,
    val dispatcher: CoroutineDispatcher,
    permits: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
        private val ISOLATION_LEVEL =
            IsolationLevel.TRANSACTION_REPEATABLE_READ // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

        /**
         * Creates a Pudding instance backed by a PostgreSQL database
         *
         * @param address      the PostgreSQL address
         * @param databaseName the database name in PostgreSQL
         * @param username     the PostgreSQL username
         * @param password     the PostgreSQL password
         * @return a [Pudding] instance backed by a PostgreSQL database
         */
        fun createPostgreSQLPudding(
            address: String,
            databaseName: String,
            username: String,
            password: String,
            permits: Int? = System.getenv("PUDDING_PERMITS")?.toIntOrNull(),
            builder: HikariConfig.() -> (Unit) = {}
        ): DatabaseStuff {
            val hikariConfig = createHikariConfig(builder)
            hikariConfig.jdbcUrl = "jdbc:postgresql://$address/$databaseName?ApplicationName=${"PowerChannelPoints"}"

            hikariConfig.username = username
            hikariConfig.password = password

            val hikariDataSource = HikariDataSource(hikariConfig)

            val cachedThreadPool = Executors.newCachedThreadPool()

            return DatabaseStuff(
                hikariDataSource,
                connectToDatabase(hikariDataSource),
                cachedThreadPool,
                // Instead of using Dispatchers.IO directly, we will create a cached thread pool.
                // This avoids issues when all Dispatchers.IO threads are blocked on transactions, causing any other coroutine using the Dispatcher.IO job to be
                // blocked.
                // Example: 64 blocked coroutines due to transactions (64 = max threads in a Dispatchers.IO dispatcher) + you also have a WebSocket listening for events, when the WS tries to
                // read incoming events, it is blocked because there isn't any available Dispatchers.IO threads!
                cachedThreadPool.asCoroutineDispatcher(),
                permits ?: (hikariDataSource.maximumPoolSize * 4)
            )
        }

        private fun createHikariConfig(builder: HikariConfig.() -> (Unit)): HikariConfig {
            val hikariConfig = HikariConfig()

            hikariConfig.driverClassName = DRIVER_CLASS_NAME

            // https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
            hikariConfig.addDataSourceProperty("reWriteBatchedInserts", "true")

            // Exposed uses autoCommit = false, so we need to set this to false to avoid HikariCP resetting the connection to
            // autoCommit = true when the transaction goes back to the pool, because resetting this has a "big performance impact"
            // https://stackoverflow.com/a/41206003/7271796
            hikariConfig.isAutoCommit = false

            // Useful to check if a connection is not returning to the pool, will be shown in the log as "Apparent connection leak detected"
            hikariConfig.leakDetectionThreshold = 30L * 1000
            hikariConfig.transactionIsolation = ISOLATION_LEVEL.name // We use repeatable read to avoid dirty and non-repeatable reads! Very useful and safe!!

            hikariConfig.maximumPoolSize = System.getenv("PUDDING_POOL_SIZE")?.toInt() ?: 4
            hikariConfig.poolName = "PuddingPool"

            hikariConfig.apply(builder)

            return hikariConfig
        }

        // Loritta (Legacy) uses this!
        fun connectToDatabase(dataSource: HikariDataSource): Database =
            Database.connect(
                dataSource,
                databaseConfig = DatabaseConfig {
                    defaultRepetitionAttempts = 5
                    defaultIsolationLevel = ISOLATION_LEVEL.levelId // Change our default isolation level
                }
            )
    }

    // Used to avoid having a lot of threads being created on the "dispatcher" just to be blocked waiting for a connection, causing thread starvation and an OOM kill
    val semaphore = Semaphore(permits)

    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: suspend Transaction.() -> T) = net.perfectdreams.exposedpowerutils.sql.transaction(
        dispatcher,
        database,
        repetitions,
        transactionIsolation,
        {
            semaphore.withPermit {
                it.invoke()
            }
        },
        statement
    )

    fun shutdown() {
        cachedThreadPool.shutdown()
    }
}