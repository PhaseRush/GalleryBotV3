package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.WordArgument
import com.phaserush.gallerybot.data.database.Row
import discord4j.core.`object`.entity.GuildMessageChannel
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty

class CommandSubmit : Command(
        "submit",
        "submit-help",
        listOf(WordArgument())
) {
    // TODO: Localize this
    override fun call(context: CommandContext): Mono<Void> {
        val contestName = arguments[0].value as String
        return context.database
                .get("select * from contests where id=? and name=?", context.event.guildId.get().asLong(), contestName)
                .next()
                .flatMap {
                    context.database.get("select * from submissions where contestName=? and guildId=? and artistId=?",
                            contestName,
                            context.event.guildId.get().asLong(),
                            context.event.member.get().id.asLong())
                            .next()
                            .map(Row::columns)
                            .flatMap { dbCols ->
                                context.event.message.channel.flatMap { c ->
                                    c.createMessage("You have already submitted artwork for $contestName!\n ${dbCols["imageUrl"]}")
                                }
                            }
                            .then()
                            .switchIfEmpty(
                                    context.event.message.channel.ofType(GuildMessageChannel::class.java)
                                            .map(GuildMessageChannel::isNsfw)
                                            .filter { context.event.message.attachments.isNotEmpty() }
                                            .flatMap {
                                                context.database.set("INSERT into submissions (contestName, guildId, artistId, isNsfw, submissionTime, imageUrl) VALUES (?,?,?,?,?,?)",
                                                        contestName,
                                                        context.event.guildId.get().asLong(),
                                                        context.event.member.get().id.asLong(),
                                                        it,
                                                        context.event.message.timestamp,
                                                        context.event.message.attachments.first()
                                                ).flatMap {
                                                    context.event.message.channel.flatMap { channel ->
                                                        channel.createMessage("Thingy submitted!!")
                                                    }
                                                }
                                            }
                                            .then()
                                            .switchIfEmpty(
                                                    context.event.message.channel.flatMap {
                                                        it.createMessage("Please attach the image you'd like to submit!")
                                                    }.then()
                                            )
                            )
                }.switchIfEmpty {
                    context.event.message.channel.flatMap {
                        it.createMessage("That contest doesn't exist!")
                    }.then()
                }
    }
}