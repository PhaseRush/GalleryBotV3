package com.phaserush.gallerybot.data.argument

import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

/**
 * An argument is a parameter passed into a command
 */
abstract class Argument<T>(
        /**
         * The message key of the name of the argument
         */
        val titleKey: String,

        /**
         * The message key of the description of the argument
         */
        val descriptionKey: String
) {
    /**
     * Parse the argument to the form required
     *
     * @param input The input to parse to the required form
     * @return The required form
     */
    abstract fun parse(event: MessageCreateEvent, input: String): Mono<T>
}