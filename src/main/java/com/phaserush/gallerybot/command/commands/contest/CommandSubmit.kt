package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.WordArgument
import com.phaserush.gallerybot.data.contest.Contest
import com.phaserush.gallerybot.data.contest.ContestSubmission
import com.phaserush.gallerybot.database
import discord4j.core.`object`.entity.GuildMessageChannel
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.time.Instant

class CommandSubmit : Command(
        "submit",
        "submit-help",
        listOf(WordArgument())
) {
    // TODO: Localize this
    override fun call(context: CommandContext): Mono<Void> {
        val contestName = context.arguments[0] as String
        return Contest.of(context.event.guildId.get(), contestName)
                .flatMap { contest ->
                    ContestSubmission.of(contestName, context.event.guildId.get(), context.event.member.get().id)
                            .flatMap { submission ->
                                context.event.message.channel.flatMap {
                                    it.createMessage("You have already submitted artwork for $contestName!\n ${submission.imageUrl}")
                                }
                            }
                            .then()
                            // empty for first time submissions
                            .switchIfEmpty( //TODO(do a permission check to make sure person is artist)
                                    // start with the channel. tried moving this down for better "logic" but wont work
                                    context.event.message.channel.ofType(GuildMessageChannel::class.java)
                                            // make sure within time interval
                                            .filter { Instant.now().isBefore(contest.submissionEndTime) }
                                            // throw informative errors, caught downstream
                                            .switchIfEmpty(Mono.error(Throwable("$contestName: Submission period has already ended!")))
                                            .filter {  Instant.now().isAfter(contest.submissionStartTime) }
                                            .switchIfEmpty(Mono.error(Throwable("$contestName: Submission period has not begun yet!")))
                                            // check that there is a image attachment
                                            .filter { context.event.message.attachments.isNotEmpty() }
                                            .switchIfEmpty(Mono.error(Throwable("No image attached, submission rejected. Try again with an image!")))
                                            .map(GuildMessageChannel::isNsfw)
                                            .flatMap {
                                                database.set("INSERT into submissions (contestName, guildId, artistId, isNsfw, submissionTime, imageUrl) VALUES (?,?,?,?,?,?)",
                                                        contestName,
                                                        context.event.guildId.get().asLong(),
                                                        context.event.member.get().id.asLong(),
                                                        it, // isNsfw
                                                        context.event.message.timestamp,
                                                        context.event.message.attachments.first()
                                                ).flatMap {
                                                    context.event.message.channel.flatMap { channel ->
                                                        channel.createMessage("Thank you; artwork successfully submitted!")
                                                    }
                                                }
                                            }
                                            .then()
                                            // errors thrown upstream caught here
                                            .onErrorResume { throwable ->
                                                context.event.message.channel.flatMap { channel ->
                                                    channel.createMessage(throwable.message!!) // might need to message!!
                                                }.then()
                                            }
                            )
                }.switchIfEmpty {
                    context.event.message.channel.flatMap {
                        it.createMessage("That contest doesn't exist! Please check your spelling and try again, " +
                                "or use `!contest view` to see currently active contests for ${context.event.guild.map { guild -> guild.name }}")
                    }.then()
                }
    }
}