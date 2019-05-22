package com.phaserush.gallerybot.data.contest

import com.phaserush.gallerybot.data.discord.UserMeta
import discord4j.core.`object`.util.Snowflake
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

data class ContestSubmission (
        // artist info
        // get infocard from infocard table using artist id as primary key
        val infoCard    : Optional<UserMeta.InfoCard>, // artist might not have one
        val contestName:String,
        val artistId : Snowflake,


        // submission info
        val messageId          : Snowflake, // used for getting message which can then be used to determine rest of info
        val isNsfw      : Boolean = false,
        val submissionDate   : LocalDate,

        // Voting period
        var numVotes: Int = 0,
        val voters: List<Snowflake> = ArrayList()// list of userId that have voted
)