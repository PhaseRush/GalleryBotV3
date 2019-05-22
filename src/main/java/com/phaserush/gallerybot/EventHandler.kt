package com.phaserush.gallerybot

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.command.CommandManager
import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.argument.Argument
import com.phaserush.gallerybot.data.database.Database
import com.phaserush.gallerybot.data.dialog.WordDialog
import com.phaserush.gallerybot.data.exceptions.BotPermissionException
import com.phaserush.gallerybot.data.exceptions.MemberPermissionException
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

class EventHandler {
    private val commandManager: CommandManager = CommandManager()
    private val database: Database = Database()
    private val localization: Localization = Localization()

    /**
     * Executes upon every message created thweat passed through the filters
     * defined in {@link ShardManager#login login}
     *
     * @param event The event context
     * @return The Mono with the command instructions
     */
    fun onMessageCreateEvent(event: MessageCreateEvent): Mono<Void> {
        val context = CommandContext(event, database, localization)
        val content = event.message.content.get()

        return context.getGuild()
                .map {
                    if (it.prefix == null) setOf(config.prefix) else setOf(config.prefix, it.prefix)
                } // Get the prefix for this guild
                .flatMap { prefixes ->
                    Mono.justOrEmpty(prefixes.firstOrNull { prefix -> content.startsWith(prefix) })
                } // Check if the message starts with one of the guild's prefixes
                .map { content.substring(it!!.length, content.length) } // Substring the prefix and the arguments out
                .filter { command -> command.isNotBlank() }
                .map { command -> commandManager.traverseThis(breakIntoList(command)) } // Find the relevant command
                .filter { it.t1 != null }
                .filterWhen {
                    it.t1?.permissions?.testBot(event)
                            ?.flatMap { set ->
                                set.isEmpty()
                                        .toMono()
                                        .filter { it }
                                        .switchIfEmpty(Mono.error(BotPermissionException(set)))
                            }
                } // Checks the necessary bot permissions, will throw error on missing permissions, handled later in the chain
                .filterWhen {
                    it.t1?.permissions?.testMember(event)
                            ?.flatMap { set ->
                                set.isEmpty()
                                        .toMono()
                                        .filter { it }
                                        .switchIfEmpty(Mono.error(MemberPermissionException(set)))
                            }
                } // Checks the user permissions, will throw error on missing permissions, handled later in the chain
                .flatMap {
                    getArguments(context, it.t1!!, it.t2)
                }
                .flatMapMany { command ->
                    event.message.channel.flatMapMany { c -> c.typeUntil(command!!.call(context)) }
                } // Execute the command and type until it finishes
                .then()
                .onErrorResume { t: Throwable ->
                    when (t) {
                        is BotPermissionException -> {
                            event.message.channel
                                    .flatMap { c ->
                                        event.guild.flatMap { g -> c.createMessage("Bot is missing permissions ${t.message!!}") }
                                    }
                                    .then()
                        } // Handle missing permissions for the bot
                        is MemberPermissionException -> {
                            event.message.channel
                                    .flatMap { c ->
                                        event.guild.flatMap { g -> c.createMessage("User is missing permissions ${t.message!!}") }
                                    }
                                    .then()
                        } // Handle missing permissions for the user
                        else -> {
                            event.message.channel
                                    .flatMap { c ->
                                        t.printStackTrace() // maybe log this instead
                                        event.guild.flatMap { g -> c.createMessage("An error or something!\n${t.message!!}") }
                                    }
                                    .then()
                        } // Handle any other exception that may have occurred
                    }
                }
    }

    private fun getArguments(context: CommandContext, command: Command, args: List<String>): Mono<Command> {
        val missingArgs: MutableList<Argument<*>> = mutableListOf()
        command.arguments
                .forEachIndexed { i, arg ->
                    if (i < args.size)
                        arg.parse(args[i])
                    else
                        missingArgs += arg
                }

        if (missingArgs.size == 0)
            return Mono.just(command)

        return Flux.fromIterable(missingArgs)
                .concatMap { argument ->
                    context.getGuild()
                            .zipWith(context.event.message.channel)
                            .flatMap {
                                WordDialog(it.t1.locale, localization, it.t2, context.event.member.get()).waitOnInput()
                                        .map { dialog -> argument.parse(dialog) }
                            }
                }
                .then(Mono.just(command))
    }

    private fun breakIntoList(breakable: String): List<String> = if (breakable == "") emptyList() else breakable.split("\\s+".toRegex())
}