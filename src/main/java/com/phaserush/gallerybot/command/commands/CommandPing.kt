package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.database
import reactor.core.publisher.Mono

class CommandPing : Command(
        "ping",
        "ping-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        return database.getGuild(context.event.guildId.get())
                .map { it.locale }
                .flatMap {
                    context.event.message.channel.flatMap { c ->
                        c.createMessage(
                                context.localization.getMessage(it, "ping", context.event.client.responseTime)
                        )
                    }
                } // testing
                .map { println("arguments:\t${arguments[0]} ${arguments[1]}") } // currently doesnt work because argument parsing is broken
                .flatMap {
                    if (context.arguments.isNotEmpty()) {
                        when (context.arguments.first() as String) {
                            "setMsgId" -> database.set("UPDATE guilds SET roleReactionMsgId = (?) WHERE id=(?) ",
                                    context.arguments[1],
                                    context.event.guildId.get().asLong())


                            "addRxnEmoji" -> database.set("INSERT INTO roleemojis values (?,?,?)",
                                    context.arguments[1],
                                    context.arguments[2],
                                    context.arguments[3])

                            else -> context.event.message.channel.flatMap { c ->
                                c.createMessage("else branch")
                            }
                        }
                    } else Mono.just(context) // else throwaway
                }
                .then()
    }
}