package com.phaserush.gallerybot.data.discord

import discord4j.core.`object`.util.Snowflake

data class UserMeta(
        val id: Snowflake,
        val isArtist : Boolean,
        val infoCard : InfoCard? // only exists if isArtist

) {
    // not sure whether to make this in here, top level of this file, or separate file
    // placeholder class for storing artist information
    data class InfoCard (
            val picUrl   : String,
            val otherUrl : String
    )
}