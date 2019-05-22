package com.phaserush.gallerybot.data.dialog

import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.Event
import discord4j.core.spec.MessageCreateSpec
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Consumer

/**
 * A dialog is a message that pops up in a channel asking for user input,
 * dialogs should only be opened by an [Argument]
 */
abstract class Dialog<T : Event, O>(
        private val clazz: Class<T>,
        private val dialog: Consumer<MessageCreateSpec>,
        val channel: MessageChannel,
        private val timeout: Duration
) {
    /**
     * Opens a dialog in a [MessageChannel], then waits for user
     * input and deletes the dialog
     *
     * @return Returns the user input
     */
    fun waitOnInput(): Mono<O> {
        return channel.createMessage(dialog)
                .flatMap { message ->
                    channel.client.eventDispatcher
                            .on(clazz)
                            .filterWhen(this::filter)
                            .take(timeout)
                            .next()
                            .flatMap {
                                message.delete()
                                        .then(processInput(it))
                            }.switchIfEmpty(
                                    message.delete()
                                            .then(Mono.empty())
                            )
                }
    }

    /**
     * Filter the undesired [MessageEvent]s
     *
     * @param event The event context
     * @return Returns true if the filters passed, false if not
     */
    abstract fun filter(event: T): Mono<Boolean>

    /**
     * Process the user input and return the relevant information
     *
     * @param event The event context
     * @return Returns the input in the relevant form
     */
    abstract fun processInput(event: T): Mono<O>
}