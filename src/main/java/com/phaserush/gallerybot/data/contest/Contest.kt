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
        private val theme: String?,
        private val winnerId: Snowflake?,

        private val submissionChannelId: Snowflake,
        private val nsfwSubmissionChannelId: Snowflake,
        private val submissionVotingChannelId: Snowflake,
        private val nsfwSubmissionVotingChannelId: Snowflake,

        // times                                           (Examples given for month of June)
        // submission
        private val themeSubmissionStartTime: Instant, // May 25 00:00
        private val themeSubmissionEndTime: Instant, // May 28 00:00
        private val themeVotingStartTime: Instant, // May 28 00:00
        private val themeVotingEndTime: Instant, // May 31 00:00

        public val submissionStartTime: Instant, // beginning of month           June 1  00:00
        public val submissionEndTime: Instant, // first moment of next month   July 1  00:00
        // submission voting
        private val votingStartTime: Instant, // first day of next month      July 1  00:00
        private val votingEndTime: Instant, // end of 3rd day of next month July 3  00:00

        // check bools
        private val themeSubmissionStartCompleted: Boolean,
        private val themeSubmissionEndCompleted: Boolean,
        private val themeVotingStartCompleted: Boolean,
        private val themeVotingEndCompleted: Boolean,
        private val submissionStartCompleted: Boolean,
        private val submissionEndCompleted: Boolean,
        private val votingStartCompleted: Boolean,
        private val votingEndCompleted: Boolean
) {
    companion object {
        fun of(guildId: Snowflake, name: String): Mono<Contest> {
            return database.get("select * from contests where id=? and name=?", guildId.asLong(), name)
                    .next()
                    .map(Row::columns)
                    .map {
                        Contest(
                                name,
                                guildId,
                                it["theme"] as String,
                                Snowflake.of(it["winnerId"] as Long),
                                Snowflake.of(it["submissionChannelId"] as Long),
                                Snowflake.of(it["nsfwSubmissionChannelId"] as Long),
                                Snowflake.of(it["submissionVotingChannelId"] as Long),
                                Snowflake.of(it["nsfwSubmissionVotingChannelId"] as Long),
                                Instant.ofEpochSecond(it["themeSubmissionStartTime"] as Long),
                                Instant.ofEpochSecond(it["themeSubmissionEndTime"] as Long),
                                Instant.ofEpochSecond(it["themeVotingStartTime"] as Long),
                                Instant.ofEpochSecond(it["themeVotingEndTime"] as Long),
                                Instant.ofEpochSecond(it["submissionStartTime"] as Long),
                                Instant.ofEpochSecond(it["submissionEndTime"] as Long),
                                Instant.ofEpochSecond(it["votingStartTime"] as Long),
                                Instant.ofEpochSecond(it["votingEndTime"] as Long),
                                it["themeSubmissionStartCompleted"] as Boolean,
                                it["themeSubmissionEndCompleted"] as Boolean,
                                it["themeVotingStartCompleted"] as Boolean,
                                it["themeVotingEndCompleted"] as Boolean,
                                it["submissionStartCompleted"] as Boolean,
                                it["submissionEndCompleted"] as Boolean,
                                it["votingStartCompleted"] as Boolean,
                                it["votingEndCompleted"] as Boolean
                        )
                    }
        }

        /**
         * take in some params,
         * then create
         */
        fun create(guildId: Snowflake, name: String,
                   submissionChannelId: Snowflake, nsfwSubmissionChannelId: Snowflake,
                   submissionVotingChannelId: Snowflake, nsfwSubmissionVotingChannelId: Snowflake,
                   themeSubmissionStartTime: Instant, themeSubmissionEndTime: Instant,
                   themeVotingStartTime: Instant, themeVotingEndTime: Instant,
                   submissionStartTime: Instant, submissionEndTime: Instant,
                   votingStartTime: Instant, votingEndTime: Instant

        ): Mono<Contest> {
            val newContest = Contest(
                    name,
                    guildId,
                    null,
                    null, // no winner at init
                    submissionChannelId,
                    nsfwSubmissionChannelId,
                    submissionVotingChannelId,
                    nsfwSubmissionVotingChannelId,
                    themeSubmissionStartTime,
                    themeSubmissionEndTime,
                    themeVotingStartTime,
                    themeVotingEndTime,
                    submissionStartTime,
                    submissionEndTime,
                    votingStartTime,
                    votingEndTime,

                    themeSubmissionStartCompleted = false,
                    themeSubmissionEndCompleted = false,
                    themeVotingStartCompleted = false,
                    themeVotingEndCompleted = false,
                    submissionStartCompleted = false,
                    submissionEndCompleted = false,
                    votingStartCompleted = false,
                    votingEndCompleted = false
            )

            return database.set("INSERT into contests values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    name,
                    guildId,
                    null,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    null,
                    submissionChannelId,
                    nsfwSubmissionChannelId,
                    submissionVotingChannelId,
                    nsfwSubmissionVotingChannelId,
                    themeSubmissionStartTime,
                    themeSubmissionEndTime,
                    themeVotingStartTime,
                    themeVotingEndTime,
                    submissionStartTime,
                    submissionEndTime,
                    votingStartTime,
                    votingEndTime)
                    .flatMap { Mono.just(newContest) }
        }
    }
}