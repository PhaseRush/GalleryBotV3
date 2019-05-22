package com.phaserush.gallerybot.data.discord

import discord4j.core.`object`.util.Snowflake
import java.util.*

data class UserMeta(
        val id      : Snowflake,
        val infoCard: Optional<InfoCard>

) {
    /**
     * Specifically for artists, and maybe patrons later on
     */
    data class InfoCard (
            val artistName:String,
            val picUrl   : String,
            val otherUrl : String
    )
}