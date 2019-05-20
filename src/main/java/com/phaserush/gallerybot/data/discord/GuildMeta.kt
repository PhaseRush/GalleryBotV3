package com.phaserush.gallerybot.data.discord

import discord4j.core.`object`.util.Snowflake
import java.util.*

class GuildMeta(
        val id: Snowflake,
        val prefix: String?,
        val locale: Locale
)