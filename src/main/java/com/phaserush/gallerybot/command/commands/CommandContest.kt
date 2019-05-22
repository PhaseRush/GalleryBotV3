package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import reactor.core.publisher.Mono

class CommandContest : Command(
        "contest",
        "contest-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return context.getGuild()
                .map { it.locale }
                .flatMap {
                    context.event.message.channel.flatMap { c ->
                        c.createMessage("Ran contest ")
                    }
                }
                .then()
    }
}