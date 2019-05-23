package com.phaserush.gallerybot.data.contest

import com.phaserush.gallerybot.data.database.Row
import com.phaserush.gallerybot.database
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono
import java.time.Instant

data class ContestSubmission(
        val contestName: String,
        val memberId: Snowflake,
        val guildId: Snowflake,

        // submission info
        val messageId: Snowflake, // used for getting message which can then be used to determine rest of info
        val isNsfw: Boolean = false,
        val submissionTime: Instant,

        val imageUrl: String,

        // Voting period
        var numVotes: Int = 0
) {
    companion object {
        fun of(name: String, guildId: Snowflake, memberId: Snowflake): Mono<ContestSubmission> {
            return database.get("select * from submissions where contestName=? and guildId=? and artistId=?", name, guildId.asLong(), memberId.asLong())
                    .next()
                    .map(Row::columns)
                    .map {
                        ContestSubmission(
                                name,
                                memberId,
                                Snowflake.of(it["guildId"] as Long),
                                Snowflake.of(it["messageId"] as Long),
                                it["isNsfw"] as Boolean,
                                Instant.ofEpochSecond(it["submissionTime"] as Long),
                                it["imageUrl"] as String,
                                it["numVotes"] as Int
                        )
                    }
        }
    }
}