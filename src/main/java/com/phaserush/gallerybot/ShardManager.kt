package com.phaserush.gallerybot

import com.phaserush.gallerybot.data.database.Row
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.event.domain.message.ReactionRemoveEvent
import discord4j.core.shard.ShardingClientBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

class ShardManager {
    private val eventHandler = EventHandler()
    private val shards: List<DiscordClient> = ShardingClientBuilder(config.token)
            .setShardCount(1)
            .build()
            .map { it.setInitialPresence(Presence.online(Activity.playing(config.presenceMessage))) }
            .map { it.build() }
            .collectList()
            .block()!!

    init {
        registerVotingStartInterval()
        registerVotingEndInterval()
        registerSubmissionStartInterval()
        registerSubmissionEndInterval()
    }

    /*private fun registerThemeVotingStartInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where themeSubmissionStartCompleted=false and unix_timestamp() >= themeSubmissionStartTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                shards[0].getChannelById(Snowflake.of(columns["submissionChannelId"] as Long))
                                        .ofType(GuildMessageChannel::class.java)
                                        .flatMap { channel ->
                                            channel.createMessage("Now accepting theme submissions!")
                                        }
                                        .flatMap { database.set("update contests set themeSubmissionStartCompleted=true where id=? and name=?", columns["id"], columns["name"]) }
                            }
                }
                .subscribe()
    }

    private fun registerThemeVotingEndInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where themeSubmissionEndCompleted=false and unix_timestamp() >= themeSubmissionEndTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                shards[0].getChannelById(Snowflake.of(columns["submissionChannelId"] as Long))
                                        .ofType(GuildMessageChannel::class.java)
                                        .flatMap { channel ->
                                            channel.createMessage("The winning theme is ") // TODO: Get theme with most votes
                                        }
                                        .flatMap { database.set("update contests set themeSubmissionEndCompleted=true where id=? and name=?", columns["id"], columns["name"]) }
                            }
                }
                .subscribe()
    }*/

    private fun registerSubmissionStartInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where submissionStartCompleted=false and unix_timestamp() >= submissionStartTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                shards[0].getChannelById(Snowflake.of(columns["submissionChannelId"] as Long))
                                        .ofType(GuildMessageChannel::class.java)
                                        .flatMap { channel ->
                                            channel.createMessage("Now accepting submissions!!! Happy drawing!!!")
                                        }
                                        .then(
                                                database.set("update contests set submissionStartCompleted=true where id=? and name=?", columns["id"], columns["name"])
                                        )
                            }
                }
                .subscribe()
    }

    private fun registerSubmissionEndInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where submissionEndCompleted=false and unix_timestamp() >= submissionEndTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                shards[0].getChannelById(Snowflake.of(columns["submissionChannelId"] as Long))
                                        .ofType(GuildMessageChannel::class.java)
                                        .flatMap { channel ->
                                            channel.createMessage("Submissions are now closed!")
                                        }
                                        .then(
                                                database.set("update contests set submissionEndCompleted=true where id=? and name=?", columns["id"], columns["name"])
                                        )
                            }
                }
                .subscribe()
    }

    private fun registerVotingStartInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where votingStartCompleted=false and unix_timestamp() >= votingStartTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                Mono.zip(
                                        shards[0].getChannelById(Snowflake.of(columns["submissionVotingChannelId"] as Long)).ofType(GuildMessageChannel::class.java),
                                        shards[0].getChannelById(Snowflake.of(columns["nsfwSubmissionVotingChannelId"] as Long)).ofType(GuildMessageChannel::class.java)
                                )
                                        .flatMap { channels ->
                                            database.get("select * from submissions where guildId=? and contestName=?", columns["id"], columns["name"])
                                                    .map(Row::columns)
                                                    .flatMap { submission ->
                                                        if (submission["isNsfw"] as Boolean)
                                                            channels.t2.createMessage(submission["imageUrl"] as String)
                                                        else
                                                            channels.t1.createMessage(submission["imageUrl"] as String)
                                                    }
                                                    .flatMap {
                                                        it.addReaction(ReactionEmoji.unicode("\u2B06"))
                                                    }
                                                    .then(
                                                            channels.t1.createMessage("Voting has started!")
                                                                    .flatMap { database.set("update contests set votingStartCompleted=true where id=? and name=?", columns["id"], columns["name"]) }
                                                    )
                                        }
                            }
                }
                .subscribe()
    }

    private fun registerVotingEndInterval() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap {
                    database.get("select * from contests where votingEndCompleted=false and unix_timestamp() >= votingEndTime")
                            .map(Row::columns)
                            .flatMap { columns ->
                                Mono.zip(
                                        database.get("select * from submissions where guildId=? and contestName=? order by numVotes desc limit 1", columns["id"], columns["name"]).next().map(Row::columns),
                                        shards[0].getChannelById(Snowflake.of(columns["submissionVotingChannelId"] as Long)).ofType(GuildMessageChannel::class.java)
                                )
                                        .flatMap { tuple ->
                                            shards[0].getUserById(Snowflake.of(tuple.t1["artistId"] as Long))
                                                    .flatMap {
                                                        tuple.t2.createMessage("${it.mention} has won the contest with ${tuple.t1["numVotes"]} votes!")
                                                    }
                                        }
                                        .then(
                                                database.set("update contests set votingEndCompleted=true where id=? and name=?", columns["id"], columns["name"])
                                        )
                            }
                }
                .subscribe()
    }

    /**
     * Logs all of the shards in and set the event listeners
     */
    fun login() {
        Mono.`when`(
                shards.map { shard ->
                    shard.login()
                            .and(shard.eventDispatcher
                                    .on(MessageCreateEvent::class.java)
                                    .filterWhen { it.message.channel.map { c -> c.type == Channel.Type.GUILD_TEXT } }
                                    .filter { it.member.isPresent }
                                    .filter { !it.member.get().isBot }
                                    .filter { it.message.content.isPresent }
                                    .flatMap { eventHandler.onMessageCreateEvent(it) }
                            )
                            .and(shard.eventDispatcher
                                    .on(ReactionAddEvent::class.java)
                                    .filterWhen { it.message.map { message -> message.author.isPresent } }
                                    .filterWhen { it.user.map { user -> !user.isBot } }
                                    .filter { it.emoji.asUnicodeEmoji().isPresent }
                                    .filter { it.emoji.asUnicodeEmoji().get().raw == "\u2B06" }
                                    .flatMap { eventHandler.onReactionAddEvent(it) }
                            )
                            .and(shard.eventDispatcher
                                    .on(ReactionRemoveEvent::class.java)
                                    .filterWhen { it.message.map { message -> message.author.isPresent } }
                                    .filterWhen { it.user.map { user -> !user.isBot } }
                                    .filter { it.emoji.asUnicodeEmoji().isPresent }
                                    .filter { it.emoji.asUnicodeEmoji().get().raw == "\u2B06" }
                                    .flatMap { eventHandler.onReactionRemoveEvent(it) }
                            )
                }
        ).block()
    }

    /**
     * Sets the presence for all of the shards
     */
    fun updatePresence(presence: Presence) {
        shards.forEach { it.updatePresence(presence).block() }
    }
}