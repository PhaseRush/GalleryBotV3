package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.database.Database
import com.phaserush.gallerybot.data.database.Row
import com.phaserush.gallerybot.data.discord.GuildMeta
import com.phaserush.gallerybot.data.discord.UserMeta
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.util.*

class CommandContext(val event: MessageCreateEvent,
                     val database: Database,
                     val localization: Localization
) {
    /**
     * Fetch the user metadata from the database
     *
     * @return The user metadata
     */
    fun getUser(): Mono<UserMeta> {
        return database.get("SELECT * FROM users WHERE id=?", event.member.get().id.asLong())
                .next()
                .map { it.columns }
                .map {
                    UserMeta(
                            event.member.get().id,
                            Optional.empty() // TODO
                    )
                }.switchIfEmpty(
                        database.set("INSERT INTO users (id) VALUES (?)", event.member.get().id.asLong())
                                .then(getUser())
                )
    }

    /**
     * Fetch the guild metadata from the database
     *
     * @return The guild metadata
     */
    fun getGuild(): Mono<GuildMeta> {
        return database.get("SELECT * FROM guilds WHERE id=?", event.guildId.get().asLong())
                .next()
                .map(Row::columns)
                .map {
                    GuildMeta(
                            event.guildId.get(),
                            it["prefix"] as String?, // attribute that corresponds to prefix column
                            Locale.forLanguageTag(it["locale"] as String)
                    )
                }.switchIfEmpty {
                    database.set("INSERT INTO guilds (id) VALUES (?)", event.guildId.get().asLong())
                            .then(getGuild())
                }
    }
}