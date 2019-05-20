package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import reactor.core.publisher.Mono
import java.util.*

class CommandPing : Command(
        "ping",
        "ping-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return context.getGuild()
                //.map { it.Locale }
                .flatMap {
                    context.event.message.channel.flatMap { c ->
                        c.createMessage(
                                context.localization.getMessage(Locale.ENGLISH, "ping", context.event.client.responseTime)
                        )
                    }
                }
                .then()
    }
}