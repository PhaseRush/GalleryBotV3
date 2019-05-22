package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import reactor.core.publisher.Mono

class CommandSubmit : Command(
        "submit",
        "submit-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        println("hello!")
        return context.event.message.channel
                .flatMap { it.createMessage("This is how you submit stuff!") }
                .then()
    }
}