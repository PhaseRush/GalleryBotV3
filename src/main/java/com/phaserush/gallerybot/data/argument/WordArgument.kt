package com.phaserush.gallerybot.data.argument

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class WordArgument : Argument<String>(
        "user-input",
        "user-input-desc"
) {
    override fun parse(event: MessageCreateEvent, input: String): Mono<String> {
        return Mono.just(input)
    }
}