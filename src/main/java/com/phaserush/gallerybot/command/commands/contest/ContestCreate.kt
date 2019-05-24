package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.WordArgument
import reactor.core.publisher.Mono

class ContestCreate : Command(
        "create",
        "create-help",
        listOf(WordArgument())
) {
    // TODO: Localize this
    override fun call(context: CommandContext): Mono<Void> {
        //val contestName = arguments[0].value as String
        return Mono.empty()
    }
}