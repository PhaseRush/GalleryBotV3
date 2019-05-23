package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono

class CommandSubmit : Command(
        "submit",
        "submit-help"
) {
    override fun call(context: CommandContext): Mono<Void> {
        val contestName = "test name" //TODO use real contest name from args
        return context.database
                .get("select * from submissions where contestName=? and guildId=? and artistId=?",
                        contestName, context.event.guildId.get().asLong(), context.event.member.get().id.asLong())
                .next()
                .map { it.columns } // there is an older submission
                .flatMap { dbCols ->
                    context.database.set("UPDATE submissions SET submissionDate=?, imageUrl=? WHERE contestName=? AND guildId=? AND artistId=?",
                            context.event.message.timestamp,
                            if (context.event.message.attachments.size == 0) throw RuntimeException("Expected attachment but none found") else context.event.message.attachments.first().url,
                            dbCols["contestName"],
                            dbCols["guildId"],
                            dbCols["artistId"]
                    ).flatMap {
                        context.event.message.channel.flatMap { c ->
                            c.createMessage("You have already submitted artwork for $contestName. Your art will be updated from:\n ${dbCols["imageUrl"]}")
                        }
                    }.flatMap {
                        // redirect submission into appropriate submission channel
                        context.database.get("SELECT * FROM contests WHERE name=? AND guildId=?",
                                contestName,
                                context.event.guildId.get().asLong())
                                .next() // assume exists because created on contest init
                                .map { db -> db.columns }
                                .map { dbCols ->
                                    context.event.message.channel.isNsfw.map {
                                        isNsfw -> if (isNsfw) dbCols["nsfwSubmissionChannelId"] as Long else dbCols["submissionChannelId"] as Long
                                    }
                                }.map { idMono ->
                                    idMono.map { id -> // or flatmap?
                                        context.event.guild.flatMap { guild -> guild.getChannelById(Snowflake.of(id)) }
                                    }
                                }.ofType(MessageChannel::class.java)
                                .flatMap { msgChannel -> msgChannel.createMessage("Artist Name submitted Name of art, thing, etc, on May 22\n URL") }
                                .then()
                    }.then()
                }.switchIfEmpty( // first submission for artist into this contest
                        context.database.set("INSERT into submissions VALUES (?,?,?,?,?,?,?)",
                                contestName,
                                context.event.guildId.get().asLong(),
                                context.event.member.get().id.asLong(),
                                context.event.message.channel.isNsfw, // isNsfw
                                context.event.message.timestamp, // instant
                                0, // init @ 0 votes
                                if (context.event.message.attachments.size == 0) throw RuntimeException("Expected attachment but none found") else context.event.message.attachments.first().url
                        ).then()
                ).onErrorContinue { t, e ->
                    context.event.message.channel.flatMap { c ->
                        c.createMessage(t.message!!)
                    }
                }.then()
    }// end fun
}

// fancy extension property
private val Mono<MessageChannel>.isNsfw: Mono<Boolean>
    get() = this.ofType(GuildMessageChannel::class.java).map { it.isNsfw }