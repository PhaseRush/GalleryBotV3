package com.phaserush.gallerybot.data.discord

import discord4j.core.`object`.util.Snowflake
import java.util.*

data class UserMeta(
        val id      : Snowflake,
        val infoCard: Optional<InfoCard>

) {
    // not sure whether to make this in here, top level of this file, or separate file
    // placeholder class for storing artist information
    // TODO: Should probably move this to a separate class
    // eh but InfoCard is a weak entity. does not exist without a User
    data class InfoCard (
            val picUrl   : String,
            val otherUrl : String
    )
}