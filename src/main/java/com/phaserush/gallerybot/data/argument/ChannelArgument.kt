package com.phaserush.gallerybot.data.argument

import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class ChannelArgument : Argument<GuildMessageChannel>("input-channel", "input-channel-desc") {
    override fun parse(event: MessageCreateEvent, input: String): Mono<GuildMessageChannel> {
        when {
            input.matches("<#([0-9]+)>".toRegex()) -> {
                return event.guild.flatMap {
                    it.getChannelById(Snowflake.of("<#([0-9]+)>".toRegex().matchEntire(input)?.groups?.get(1)?.value!!))
                            .ofType(GuildMessageChannel::class.java)
                            .switchIfEmpty(Mono.error(IllegalArgumentException("That channel doesn't exist")))
                }
            }
            input.matches("[0-9]+".toRegex()) -> {
                return event.guild.flatMap { guild ->
                    guild.channels
                            .filter { it.id.asString() == input }
                            .next()
                            .ofType(GuildMessageChannel::class.java)
                            .switchIfEmpty(Mono.error(IllegalArgumentException("Could not find a channel by that ID")))
                }
            }
            else -> {
                return event.guild.flatMap { guild ->
                    guild.channels
                            .filter { it.name == input }
                            .next()
                            .ofType(GuildMessageChannel::class.java)
                            .switchIfEmpty(Mono.error(IllegalArgumentException("Could not find a channel by that name")))
                }
            }
        }
    }
}