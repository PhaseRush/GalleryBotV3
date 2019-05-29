package com.phaserush.gallerybot.data.database

import com.phaserush.gallerybot.config
import com.phaserush.gallerybot.data.discord.GuildMeta
import com.phaserush.gallerybot.data.discord.UserMeta
import com.phaserush.gallerybot.database
import com.zaxxer.hikari.HikariDataSource
import discord4j.core.`object`.util.Snowflake
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.sql.PreparedStatement
import java.sql.ResultSetMetaData
import java.sql.Types
import java.time.Instant
import java.util.*

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
    fun set(@Language("MariaDB") sql: String, vararg args: Any?): Mono<Int> {
        return Mono.fromCallable {
            poolingDataSource.connection.use { con ->
                prepareStatement(con.prepareStatement(sql), *args)
                        .executeUpdate()
            }
        }
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
                is Boolean -> statement.setBoolean(i, arg)
                is Instant -> statement.setLong(i, arg.epochSecond)
            }
        }
        return statement
    }

    /**
     * Fetch the user metadata from the database
     *
     * @return The user metadata
     */
    fun getUser(id: Snowflake): Mono<UserMeta> {
        return database.get("SELECT * FROM users WHERE id=?", id.asLong())
                .next()
                .map { it.columns }
                .map {
                    UserMeta(
                            id,
                            Optional.empty() // TODO
                    )
                }.switchIfEmpty {
                    database.set("INSERT INTO users (id) VALUES (?)", id.asLong())
                            .then(getUser(id))
                }
    }

    /**
     * Fetch the guild metadata from the database
     *
     * @return The guild metadata
     */
    fun getGuild(id: Snowflake): Mono<GuildMeta> {
        return database.get("SELECT * FROM guilds WHERE id=?", id.asLong())
                .next()
                .map(Row::columns)
                .map {
                    GuildMeta(
                            id,
                            it["prefix"] as String?, // attribute that corresponds to prefix column
                            Locale.forLanguageTag(it["locale"] as String),
                            if (it["roleReactionMsgId"] == null) null else Snowflake.of(it["roleReactionMsgId"] as Long)
                    )
                }.switchIfEmpty {
                    database.set("INSERT INTO guilds (id) VALUES (?)", id.asLong())
                            .then(getGuild(id))
                }
    }

    /**
     * This will be used to create the necessary tables and schemas for the bot, pls dun touch *poke*
     */
    private fun setup() {
        set("CREATE TABLE IF NOT EXISTS guilds(id BIGINT PRIMARY KEY NOT NULL, prefix VARCHAR(12) DEFAULT NULL, locale VARCHAR(5) NOT NULL DEFAULT 'en-US', roleReactionMsgId BIGINT)")
                .block()
        logger.info("Guild table created")
        set("CREATE TABLE IF NOT EXISTS users(id BIGINT PRIMARY KEY NOT NULL, infoCardUrl VARCHAR(1024) DEFAULT NULL)")
                .block()
        logger.info("Users table created")
        set("CREATE TABLE IF NOT EXISTS infoCards(artistId BIGINT NOT NULL PRIMARY KEY, artistName VARCHAR(30) NOT NULL DEFAULT 'Artist', otherUrl VARCHAR(200) NOT NULL DEFAULT 'https://i.pinimg.com/736x/56/1c/05/561c05cddc8a57c093203b31539d09eb.jpg', picUrl VARCHAR(200) NOT NULL DEFAULT 'https://i.pinimg.com/736x/56/1c/05/561c05cddc8a57c093203b31539d09eb.jpg', CONSTRAINT fk_infocard_onetoone FOREIGN KEY (artistId) REFERENCES users(id) ON DELETE CASCADE)")
                .block()
        set("CREATE TABLE IF NOT EXISTS contests(name VARCHAR(30) NOT NULL, id BIGINT NOT NULL, theme VARCHAR(200) NOT NULL, themeSubmissionStartCompleted BOOLEAN NOT NULL DEFAULT FALSE, themeSubmissionEndCompleted BOOLEAN NOT NULL DEFAULT FALSE, themeVotingStartCompleted BOOLEAN NOT NULL DEFAULT FALSE, themeVotingEndCompleted BOOLEAN NOT NULL DEFAULT FALSE, submissionStartCompleted BOOLEAN NOT NULL DEFAULT FALSE, submissionEndCompleted BOOLEAN NOT NULL DEFAULT FALSE, votingStartCompleted BOOLEAN NOT NULL DEFAULT FALSE, votingEndCompleted BOOLEAN NOT NULL DEFAULT FALSE, winnerId BIGINT DEFAULT NULL, submissionChannelId BIGINT NOT NULL, nsfwSubmissionChannelId BIGINT DEFAULT NULL, submissionVotingChannelId BIGINT NOT NULL, nsfwSubmissionVotingChannelId BIGINT NOT NULL, themeSubmissionStartTime BIGINT NOT NULL, themeSubmissionEndTime BIGINT NOT NULL, themeVotingStartTime BIGINT NOT NULL, themeVotingEndTime BIGINT NOT NULL, submissionStartTime BIGINT NOT NULL , submissionEndTime BIGINT NOT NULL, votingStartTime BIGINT NOT NULL, votingEndTime BIGINT NOT NULL, PRIMARY KEY(name, id))")
                .block()
        set("CREATE TABLE IF NOT EXISTS submissions(contestName VARCHAR(30) NOT NULL, guildId BIGINT NOT NULL, artistId BIGINT NOT NULL, isNsfw BOOLEAN NOT NULL DEFAULT FALSE, submissionTime BIGINT NOT NULL, numVotes INT NOT NULL DEFAULT 0, imageUrl VARCHAR(200) NOT NULL, PRIMARY KEY(contestName, guildId, artistId), CONSTRAINT fk_submission_weak_entity FOREIGN KEY (contestName, guildId) REFERENCES contests(name, id) ON DELETE CASCADE)")
                .block()
        set("CREATE TABLE IF NOT EXISTS roleEmojis (guildId BIGINT, name VARCHAR(30), roleId BIGINT NOT NULL , PRIMARY KEY (guildId, name), CONSTRAINT fkEmoteWeakEntity FOREIGN KEY (guildID) REFERENCES guilds(id)) ")
                .block()
        // proper db structure would require this
//        set("CREATE TABLE IF NOT EXISTS roleReactionMsgId(guildId BIGINT PRIMARY KEY, messageId BIGINT, CONSTRAINT fk_reactionMsg_weak_entity FOREIGN KEY (guildId) REFERENCES guilds(id) ON DELETE CASCADE)")
//                .block()
    }
}