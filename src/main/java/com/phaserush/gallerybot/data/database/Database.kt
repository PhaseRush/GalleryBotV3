package com.phaserush.gallerybot.data.database

import com.phaserush.gallerybot.config
import com.zaxxer.hikari.HikariDataSource
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.sql.PreparedStatement
import java.sql.ResultSetMetaData

class Database {
    private val logger: Logger = LoggerFactory.getLogger(Database::class.java)
    private val poolingDataSource: HikariDataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:mysql://${config.databaseIp}:${config.databasePort}/${config.databaseName}"
        username = config.databaseUsername
        password = config.databasePassword
    }

    init {
        if (!setup()) {
            logger.error("Failed to connect to database. Are you sure you used the right credentials?")
            System.exit(-1)
        }
    }

    /**
     * Executes an update statement in the database
     *
     * @param sql The sql statement to execute
     * @return Returns a Mono<Void>
     */
    fun set(@Language("MariaDB") sql: String, vararg args: Any): Mono<Void> {
        return Mono.fromCallable {
            poolingDataSource.connection.use { con ->
                prepareStatement(con.prepareStatement(sql), *args)
                        .executeUpdate()
            }
        }.then()
    }

    /**
     * Queries the database and returns the resulting rows
     *
     * @param sql The SQL query to execute
     * @args The arguments for the prepared statement
     * @return Returns all the resulting rows from the query
     */
    fun get(@Language("MariaDB") sql: String, vararg args: Any): Flux<Row> {
        return Flux.create { sink ->
            poolingDataSource.connection.use { con ->
                prepareStatement(con.prepareStatement(sql), *args).use { statement ->
                    statement.executeQuery().use { set ->
                        while (set.next()) {
                            val row = Row()
                            val metadata: ResultSetMetaData = set.metaData
                            val columns: Int = metadata.columnCount

                            for (i in 1..columns) {
                                row.addColumn(metadata.getColumnName(i), set.getObject(i))
                            }
                            sink.next(row)
                        }
                        sink.complete()
                    }
                }
            }
        }
    }

    /**
     * Prepares a prepared statement, sets all the objects in the statement
     *
     * @param statement The prepared statement to prepare
     * @param args The arguments to fill into the statement
     * @return The actually prepared prepared statement
     */
    private fun prepareStatement(statement: PreparedStatement, vararg args: Any): PreparedStatement {
        for (i in 1..args.size) {
            statement.setObject(i, args[i - 1])
        }
        return statement
    }

    /**
     * This will be used to create the necessary tables and schemas for the bot, pls dun touch
     */
    private fun setup(): Boolean {
        return true
    }
}