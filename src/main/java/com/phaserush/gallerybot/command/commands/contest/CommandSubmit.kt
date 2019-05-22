package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.contest.Contest
import reactor.core.publisher.Mono
import reactor.core.publisher.switchIfEmpty
import java.lang.RuntimeException

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
                    ).then(context.event.message.channel.flatMap { c ->
                        c.createMessage("You have already submitted artwork for $contestName:\n ${dbCols["imageUrl"]}")
                    })
                }.switchIfEmpty ( // first submission for artist into this contest
                        context.database.set("INSERT into submissions VALUES (?,?,?,?,?,?,?)",
                                contestName,
                                context.event.guildId.get().asLong(),
                                context.event.member.get().id.asLong(),
                                context.event.message.channel.block().type(), // isNsfw
                                context.event.message.timestamp,
                                0,
                                if (context.event.message.attachments.size == 0) throw RuntimeException("Expected attachment but none found") else context.event.message.attachments.first().url
                        ).then()
                )
                .onErrorContinue{ t, e ->
                    context.event.message.channel.flatMap { c ->
                        c.createMessage(t.message!!)
                    }}
                .then()
    }// end fun
}
