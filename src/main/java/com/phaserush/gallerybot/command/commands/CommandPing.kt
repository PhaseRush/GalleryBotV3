package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.database
import reactor.core.publisher.Mono

class CommandPing : Command(
        "ping",
        "ping-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return database.getGuild(context.event.guildId.get())
                .map { it.locale }
                .flatMap {
                    context.event.message.channel.flatMap { c ->
                        c.createMessage(
                                context.localization.getMessage(it, "ping", context.event.client.responseTime)
                        )
                    }
                }
                .then()
    }
}