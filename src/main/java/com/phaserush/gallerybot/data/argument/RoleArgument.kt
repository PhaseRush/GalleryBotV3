package com.phaserush.gallerybot.data.argument

import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class RoleArgument : Argument<Role>(
        "user-input",
        "user-input-desc"
) {
    override fun parse(event: MessageCreateEvent, input: String): Mono<Role> {
        return Mono.just(event.client)
                .map { println(input.replace("[^0-9]", "")) }
                .flatMap { client ->
                    event.client.getRoleById(
                            event.guildId.get(),
                            Snowflake.of(input.replace("[^0-9]".toRegex(), "").toLong()))
                } // do stuff on error maybe
    }
}