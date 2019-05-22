package com.phaserush.gallerybot.data.contest

import com.phaserush.gallerybot.data.discord.UserMeta
import discord4j.core.`object`.util.Snowflake
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
/*
create table submissions(
contestName VARCHAR(30),
guildId BIGINT,
artistId BIGINT,
messageId BIGINT,
isNsfw TINYINT,
submissionDate TIMESTAMP,
numVotes INT,
imageUrl VARCHAR(300) NOT NULL,
CONSTRAINT `fk_submission_weak_entity`
	FOREIGN KEY (`contestName`, `guildId`) REFERENCES Contests(`name`, `guildId`)
	ON DELETE CASCADE
	On UPDATE RESTRICT,
PRIMARY KEY(contestName, guildId, artistId)
);
 */
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
        var numVotes    : Int = 0,
        val voters      : List<Long> = ArrayList()// list of userId that have voted
)