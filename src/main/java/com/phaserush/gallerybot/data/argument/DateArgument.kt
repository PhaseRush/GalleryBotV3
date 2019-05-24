package com.phaserush.gallerybot.data.argument

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.time.*
import java.time.temporal.ChronoUnit

class DateArgument : Argument<Instant>("input-date", "input-date-desc") {
    override fun parse(event: MessageCreateEvent, input: String): Mono<Instant> {
        if ("(\\d\\d?)/(\\d\\d?)".toRegex().matches(input)) {
            val groups = "(\\d\\d?)/(\\d\\d?)".toRegex().matchEntire(input)?.groups?.map { it?.value }
            val dateNow =
                    Instant.from(
                            ZonedDateTime.of(
                                    LocalDateTime.of(
                                            LocalDate.of(
                                                    Year.now().value,
                                                    groups!![1]!!.toInt(),
                                                    groups[2]!!.toInt()
                                            ),
                                            LocalTime.MIDNIGHT
                                    ),
                                    ZoneId.of("America/Los_Angeles")
                            )
                    )
            return Mono.just(if (Instant.now().isAfter(dateNow)) dateNow.plus(365, ChronoUnit.DAYS) else Instant.from(dateNow))
        } else {
            throw IllegalArgumentException("Please use the date format `dd/MM`")
        }
    }
}