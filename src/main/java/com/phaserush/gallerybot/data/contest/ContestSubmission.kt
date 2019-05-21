package com.phaserush.gallerybot.data.contest

import com.phaserush.gallerybot.data.discord.UserMeta
import discord4j.core.`object`.util.Snowflake
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

data class ContestSubmission(
        // artist info
        val infoCard: Optional<UserMeta.InfoCard>, // artist might not have one

        // submission info
        val id: Snowflake, // used for getting message which can then be used to determine rest of info
        val isNSFW: Boolean = false,
        val submissionDate: LocalDate,

        // Voting period
        var numVotes: Int = 0,
        val voters: List<Snowflake> = ArrayList()// list of userId that have voted
)