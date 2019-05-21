package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.Node
import reactor.core.publisher.Mono

class CommandContest : Command(
        "contest",
        "contest-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return Mono.empty() // TODO Doesn't do anything right now but should probably display some help
    }
}