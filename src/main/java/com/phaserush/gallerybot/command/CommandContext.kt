package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.data.Localization
import com.phaserush.gallerybot.data.argument.Argument
import com.phaserush.gallerybot.data.database.Database
import com.phaserush.gallerybot.data.database.Row
import com.phaserush.gallerybot.data.discord.GuildMeta
import com.phaserush.gallerybot.data.discord.UserMeta
import com.phaserush.gallerybot.database
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.util.*

data class CommandContext(
        val event: MessageCreateEvent,
        val localization: Localization,
        val arguments: List<Any>
)