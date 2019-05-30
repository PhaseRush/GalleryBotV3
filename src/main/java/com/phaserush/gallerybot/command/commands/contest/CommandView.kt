package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.database
import discord4j.core.`object`.entity.GuildMessageChannel
import reactor.core.publisher.Mono

class CommandView : Command(
        "view",
        "view-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return context.event.message.channel
                .ofType(GuildMessageChannel::class.java)
                .flatMap { channel ->
                    database.get("select name from contests where id=?", context.event.guildId.get().asLong())
                            .map { it.columns["name"] as String }
                            .collectList()
                            .flatMap { contests ->
                                channel.createMessage("The current active contests are: " + contests.joinToString())
                            }
                            .then()
                }
    }
}