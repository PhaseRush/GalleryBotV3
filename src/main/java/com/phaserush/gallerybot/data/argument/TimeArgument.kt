package com.phaserush.gallerybot.data.argument

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.time.Instant

class TimeArgument : Argument<Instant>("input-time", "input-time-desc") {
    override fun parse(event: MessageCreateEvent, input: String): Mono<Instant> {
        if ("(\\d\\d?):(\\d\\d?)".toRegex().matches(input)) {
            val groups = "(\\d\\d?):(\\d\\d?)".toRegex().matchEntire(input)!!.groups
            val hours = groups[1]!!.value.toLong()
            val minutes = groups[2]!!.value.toLong()

            if (hours > 23 || minutes > 59)
                throw IllegalArgumentException("You cannot specify a time above 23:59")

            return Mono.just(Instant.ofEpochSecond(hours * 60 * 60 + minutes * 60))
        } else {
            throw IllegalArgumentException("Please use the format HH:mm")
        }
    }
}