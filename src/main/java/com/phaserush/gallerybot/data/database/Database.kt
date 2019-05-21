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
import java.sql.Types

class Database {
    private val logger: Logger = LoggerFactory.getLogger(Database::class.java)
    private val poolingDataSource: HikariDataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:mysql://${config.databaseIp}:${config.databasePort}/${config.databaseName}?serverTimezone=UTC"
        username = config.databaseUsername
        password = config.databasePassword
    }

    init {
        setup()
    }

    /**
     * Executes an update statement in the database
     *
     * @param sql The sql statement to execute
     * @return Returns a Mono<Void>
     */
    fun set(@Language("MariaDB") sql: String, vararg args: Any?): Mono<Void> {
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
    fun get(@Language("MariaDB") sql: String, vararg args: Any?): Flux<Row> {
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
    private fun prepareStatement(statement: PreparedStatement, vararg args: Any?): PreparedStatement {
        for (i in 1..args.size) {
            when (val arg: Any? = args[i - 1]) {
                is String? -> if (arg == null) statement.setNull(i, Types.VARCHAR) else statement.setString(i, arg)
                is Long -> statement.setLong(i, arg)
                else -> println("hello! $i")
            }
            /*when (arg) {
                is String? -> if (arg == null) statement.setNull(i, Types.VARCHAR) else statement.setString(i, arg)
                is Long? -> if (arg == null) statement.setNull(i, Types.BIGINT) else statement.setLong(i, arg)
                else -> println("You idiot $i")
            }*/
        }
        return statement
    }

    /**
     * This will be used to create the necessary tables and schemas for the bot, pls dun touch *poke*
     */
    private fun setup() {
        set("CREATE TABLE IF NOT EXISTS guilds(id BIGINT PRIMARY KEY NOT NULL, prefix VARCHAR(12) DEFAULT NULL, locale VARCHAR(5) NOT NULL DEFAULT 'en-US')")
                .block()
        set("CREATE TABLE IF NOT EXISTS users(id BIGINT PRIMARY KEY NOT NULL, infoCardUrl VARCHAR(1024) DEFAULT NULL)")
                .block()
    }
}