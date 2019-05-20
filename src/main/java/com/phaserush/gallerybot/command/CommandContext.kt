package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.database.Database
import com.phaserush.gallerybot.data.discord.GuildMeta
import com.phaserush.gallerybot.data.discord.UserMeta
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
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
        return Mono.empty() // TODO
    }

    /**
     * Fetch the guild metadata from the database
     *
     * @return The guild metadata
     */
    fun getGuild(): Mono<GuildMeta> {
        return Mono.just(GuildMeta(event.guildId.get(), null, Locale.ENGLISH)) // TODO
    }
}