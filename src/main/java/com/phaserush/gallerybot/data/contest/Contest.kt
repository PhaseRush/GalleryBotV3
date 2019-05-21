package com.phaserush.gallerybot.data.contest

import discord4j.core.`object`.util.Snowflake
import java.time.Month
import java.time.ZonedDateTime
import java.util.*

data class Contest (
        // metadata
        private val uuid    : UUID = UUID.randomUUID(),
        private val month   : Month,
        private val theme   : String,
        private val timezone: TimeZone, // maybe use SimpleTimeZone
        private val winnerId: Snowflake,
        private val isDone  : Boolean,

        // contest info
        private val submissions : MutableList<ContestSubmission>,

        // times                                           (Examples given for month of June)
        // submission
        private val submissionStartTime     : ZonedDateTime, // beginning of month           June 1  00:00
        private val submissionEndTime       : ZonedDateTime, // first moment of next month   July 1  00:00
        // submission voting
        private val votingStartTime         : ZonedDateTime, // first day of next month      July 1  00:00
        private val votingEndTime           : ZonedDateTime, // end of 3rd day of next month July 3  00:00
        // theme
        private val themeSubmissionStartTIme: ZonedDateTime, // 6 days before end of month   June 25 00:00
        private val themeSubmissionEndTime  : ZonedDateTime, // 3 days before end of month   June 28 00:00
        // theme voting
        private val themeVotingStartTime    : ZonedDateTime, // 3 days before end of month   June 28 00:00
        private val themeVotingEndTime      : ZonedDateTime  // last day of month            June 30 00:00
)