package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.database
import reactor.core.publisher.Mono

class CommandContest : Command(
        "contest",
        "contest-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return database.getGuild(context.event.guildId.get())
                .map { it.locale }
                .flatMap {
                    context.event.message.channel.flatMap { c ->
                        c.createMessage("Ran contest ")
                    }
                }
                .then()
    }
}