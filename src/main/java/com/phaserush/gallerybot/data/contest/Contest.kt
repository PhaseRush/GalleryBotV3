package com.phaserush.gallerybot.data.contest

import com.phaserush.gallerybot.data.database.Row
import com.phaserush.gallerybot.database
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import java.time.Instant

data class Contest(
        // metadata
        private val name: String,
        private val id: Snowflake,
        private val theme: String,
        private val winnerId: Snowflake,
        private val isCompleted: Boolean,

        private val submissionChannelId: Snowflake,
        private val nsfwSubmissionChannelId: Snowflake,

        // times                                           (Examples given for month of June)
        // submission
        private val themeSubmissionStartTime: Instant, // May 25 00:00
        private val themeSubmissionEndTime: Instant, // May 28 00:00
        private val themeVotingStartTime: Instant, // May 28 00:00
        private val themeVotingEndTime: Instant, // May 31 00:00

        private val submissionStartTime: Instant, // beginning of month           June 1  00:00
        private val submissionEndTime: Instant, // first moment of next month   July 1  00:00
        // submission voting
        private val votingStartTime: Instant, // first day of next month      July 1  00:00
        private val votingEndTime: Instant // end of 3rd day of next month July 3  00:00
) {
    companion object {
        fun of(id: Snowflake, name: String): Mono<Contest> {
            return database.get("select * from contests where id=? and name=?", id.asLong(), name)
                    .next()
                    .map(Row::columns)
                    .map {
                        Contest(
                                name,
                                id,
                                it["theme"] as String,
                                Snowflake.of(it["winnerId"] as Long),
                                it["completed"] as Boolean,
                                Snowflake.of(it["submissionChannelId"] as Long),
                                Snowflake.of(it["nsfwSubmissionChannelId"] as Long),
                                Instant.ofEpochSecond(it["themeSubmissionStartTime"] as Long),
                                Instant.ofEpochSecond(it["themeSubmissionEndTime"] as Long),
                                Instant.ofEpochSecond(it["themeVotingStartTime"] as Long),
                                Instant.ofEpochSecond(it["themeVotingEndTime"] as Long),
                                Instant.ofEpochSecond(it["submissionStartTime"] as Long),
                                Instant.ofEpochSecond(it["submissionEndTime"] as Long),
                                Instant.ofEpochSecond(it["votingStartTime"] as Long),
                                Instant.ofEpochSecond(it["votingEndTime"] as Long)
                        )
                    }
        }
    }
}