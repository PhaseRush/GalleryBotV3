package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import reactor.core.publisher.Mono

class CommandEmpty : Command(
        "empty",
        "empty-none"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return Mono.empty()
    }

}