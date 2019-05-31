package com.phaserush.gallerybot

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.command.CommandManager
import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.database.Row
import com.phaserush.gallerybot.data.dialog.WordDialog
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class EventHandler {
    private val commandManager: CommandManager = CommandManager()
    private val localization: Localization = Localization()

    private val logger: Logger = LoggerFactory.getLogger(EventHandler::class.java)

    fun onReactionAddEvent(event: ReactionAddEvent): Mono<Void> {
        return database.get("select * from contests where submissionVotingChannelId=? or nsfwSubmissionVotingChannelId=?", event.channelId.asLong(), event.channelId.asLong())
                .next()
                .map(Row::columns)
                .flatMap { columns ->
                    event.message.flatMap { message ->
                        database.set("update submissions set numVotes = numVotes + 1 where contestName=? and imageUrl=? and guildId=?", columns["name"], message.content.get(), event.guildId.get().asLong())
                    }
                }
                .then()
    }

    fun onReactionRemoveEvent(event: ReactionRemoveEvent): Mono<Void> {
        return database.get("select * from contests where submissionVotingChannelId=? or nsfwSubmissionVotingChannelId=?", event.channelId.asLong(), event.channelId.asLong())
                .next()
                .map(Row::columns)
                .flatMap { columns ->
                    event.message.flatMap { message ->
                        database.set("update submissions set numVotes = numVotes - 1 where contestName=? and imageUrl=? and guildId=?", columns["name"], message.content.get(), event.guildId.get().asLong())
                    }
                }
                .then()
    }

    /**
     * Executes upon every message created thweat passed through the filters
     * defined in [ShardManager.login]]}
     *
     * @param event The event context
     * @return The Mono with the command instructions
     */
    fun onMessageCreateEvent(event: MessageCreateEvent): Mono<Void> {
        return database.getGuild(event.guildId.get())
                .map { if (it.prefix != null) setOf(it.prefix, config.prefix) else setOf(config.prefix) }
                .flatMap { Mono.justOrEmpty(it.firstOrNull { prefix -> event.message.content.get().startsWith(prefix) }) }
                .map { event.message.content.get().substring(it!!.length, event.message.content.get().length) }
                .filter { it.isNotEmpty() }
                .map { string ->
                    commandManager.traverseThis(breakIntoList(string))
                            .mapT1 { it!! }
                }
                .filter {
                    it.t1.name != "empty" // filter the empty case for node traversal (Tuple cannot contain null)
                }
                .flatMap { tuple ->
                    tuple.t1.permissions.testBot(event)
                            .filter { it.isNotEmpty() }
                            .flatMap {
                                event.message.channel
                                        .flatMap { channel ->
                                            channel.createMessage("I'm missing the following permissions: ${it.joinToString()}")
                                        }
                            }
                            .then()
                            .switchIfEmpty(
                                    tuple.t1.permissions.testMember(event)
                                            .filter { it.isNotEmpty() }
                                            .flatMap {
                                                event.message.channel
                                                        .flatMap { channel ->
                                                            channel.createMessage("You're missing the following permissions: ${it.joinToString()}")
                                                        }
                                            }
                                            .then()
                                            .switchIfEmpty(
                                                    getArguments(event, tuple.t1, tuple.t2)
                                                            .collectList()
                                                            .flatMap {
                                                                tuple.t1.call(CommandContext(event, localization, it)) // TODO: This is broken
                                                            }
                                            )
                            )
                }
                .then()
                .onErrorResume { t ->
                    when (t) {
                        is IllegalArgumentException -> {
                            event.message.channel
                                    .flatMap {
                                        it.createMessage(t.message!!)
                                    }
                                    .then()
                        }
                        else -> {
                            t.printStackTrace()
                            Mono.empty()
                        }
                    }
                }
    }

    /**
     * function for artist role assignment
     */
//    fun onReactionAddEvent(event: ReactionAddEvent): Mono<Void> {
//        return Mono.just(event)
//                .map { it.messageId.asLong() }
//                .filter { }
//    }


    private fun getArguments(event: MessageCreateEvent, command: Command, args: List<String>): Flux<Any> {
        return Flux.fromIterable(command.arguments)
                .index()
                .concatMap {
                    Mono.justOrEmpty(args.getOrNull(it.t1.toInt()))
                            .flatMap { arg -> it.t2.parse(event, arg!!) }
                            .switchIfEmpty(
                                    database.getGuild(event.guildId.get())
                                            .flatMap { meta ->
                                                event.message.channel.flatMap { channel ->
                                                    WordDialog(meta.locale, localization, channel, event.member.get())
                                                            .waitOnInput()
                                                            .flatMap { input -> it.t2.parse(event, input) }
                                                }
                                            }
                            )
                }
    }

    private fun breakIntoList(breakable: String): List<String> = if (breakable == "") emptyList() else breakable.split("\\s+".toRegex())
}