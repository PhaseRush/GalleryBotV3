package com.phaserush.gallerybot.command.commands

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.RoleArgument
import com.phaserush.gallerybot.data.argument.WordArgument
import com.phaserush.gallerybot.database
import discord4j.core.`object`.entity.Role
import reactor.core.publisher.Mono

class CommandRoleEmojiAssign : Command(
        "assignEmoji",
        "assignEmoji-help",
        listOf(WordArgument(), RoleArgument())
) {
    override fun call(context: CommandContext): Mono<Void> {
        return database.set("INSERT INTO roleemojis values (?,?,?)",
                context.event.guildId.get().asLong(),
                context.arguments[0], // incorrect, need to parse more
                (context.arguments[1] as Role).id.asLong())
                .map { println("run") }
                .then()
    }
}