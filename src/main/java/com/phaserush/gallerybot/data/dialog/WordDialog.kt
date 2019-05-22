package com.phaserush.gallerybot.data.dialog

import com.phaserush.gallerybot.data.Localization
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import java.awt.Color
import java.time.Duration
import java.util.*
import java.util.function.Consumer

/**
 * This type of dialog waits for user input in chat
 * and returns only 1 word
 */
class WordDialog(
        locale: Locale,
        localization: Localization,
        channel: MessageChannel,
        private val member: Member
) : Dialog<MessageCreateEvent, String>(
        MessageCreateEvent::class.java,
        Consumer {
            it.setEmbed { embed ->
                embed.setTitle(localization.getMessage(locale, "word-dialog-title"))
                        .setDescription(localization.getMessage(locale, "word-dialog-desc"))
                        .setColor(Color.PINK)
            }
        },
        channel,
        Duration.ofSeconds(3)
) {
    override fun filter(event: MessageCreateEvent): Mono<Boolean> {
        return event.message.channel.map {
            it == channel &&
                    event.message.content.isPresent &&
                    event.member.isPresent &&
                    event.member.map(Member::isBot).map(Boolean::not).orElse(false) &&
                    event.member.get() == member
        }
    }

    override fun processInput(event: MessageCreateEvent): Mono<String> {
        return Mono.just(event.message.content.get().split(' ')[0])
    }
}