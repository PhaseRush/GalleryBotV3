package com.phaserush.gallerybot.data.argument

import com.phaserush.gallerybot.command.CommandContext
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException

class ChannelArgument : Argument<GuildMessageChannel>("input-channel", "input-channel-desc") {
    override fun parse(event: MessageCreateEvent, input: String): Mono<GuildMessageChannel> {
        when {
            input.matches("<#([0-9])+>".toRegex()) -> {
                return event.guild.flatMap {
                    it.getChannelById(Snowflake.of(input.split("<#([0-9]+)>".toRegex())[0]))
                            .ofType(GuildMessageChannel::class.java)
                }
            }
            input.matches("[0-9]+".toRegex()) -> {
                return event.guild.flatMap { guild ->
                    guild.channels.filter { it.id.asString() == input }
                            .next()
                            .ofType(GuildMessageChannel::class.java)
                }
            }
            else -> {
                return event.guild.flatMap {
                    it.channels.filter { it.name == input }.
                            next()
                            .ofType(GuildMessageChannel::class.java)
                }
            }
        }
    }
}