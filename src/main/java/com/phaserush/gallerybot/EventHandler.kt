package com.phaserush.gallerybot

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.command.CommandManager
import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.argument.Argument
import com.phaserush.gallerybot.data.dialog.WordDialog
import com.phaserush.gallerybot.data.discord.GuildMeta
import com.phaserush.gallerybot.data.exceptions.BotPermissionException
import com.phaserush.gallerybot.data.exceptions.MemberPermissionException
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

class EventHandler {
    private val commandManager: CommandManager = CommandManager()
    private val localization: Localization = Localization()

    private val logger: Logger = LoggerFactory.getLogger(EventHandler::class.java)

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
                .map { it.firstOrNull { prefix -> event.message.content.get().startsWith(prefix) } }
                .map { event.message.content.get().substring(it!!.length, event.message.content.get().length) }
                .filter { it.isNotEmpty() }
                .map { string ->
                    commandManager.traverseThis(breakIntoList(string))
                            .mapT1 { it!! }
                }
                .flatMap { tuple ->
                    getArguments(event, tuple.t1, tuple.t2)
                            .collectList()
                            .flatMap { tuple.t1.call(CommandContext(event, localization, it)) }
                }
                .then()
                .onErrorResume { t ->
                    println("FML: " + t.message)
                    when (t) {
                        else -> {
                            event.message.channel.flatMap {
                                it.createMessage(t.message)
                            }.then()
                        }
                    }
                }
    }

    private fun getArguments(event: MessageCreateEvent, command: Command, args: List<String>): Flux<Any> {
        return Flux.fromIterable(command.arguments)
                .index()
                .filter { args.size < it.t1.toInt() }
                .concatMap { tuple ->
                    tuple.t2.parse(event, args[tuple.t1.toInt()])
                }
                .switchIfEmpty(
                        event.message.channel.flatMap {
                            it.createMessage("missing arg!!")
                        }
                )
                .map { it!! }
    }

    private fun breakIntoList(breakable: String): List<String> = if (breakable == "") emptyList() else breakable.split("\\s+".toRegex())
}