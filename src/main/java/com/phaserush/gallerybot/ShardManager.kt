package com.phaserush.gallerybot

import com.phaserush.gallerybot.data.database.Row
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.entity.GuildMessageChannel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.shard.ShardingClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

class ShardManager {
    private val logger: Logger = LoggerFactory.getLogger(ShardManager::class.java)
    private val eventHandler = EventHandler()
    private val shards: List<DiscordClient> = ShardingClientBuilder(config.token)
            .setShardCount(1)
            .build()
            .map { it.setInitialPresence(Presence.online(Activity.playing(config.presenceMessage))) }
            .map { it.build() }
            .collectList()
            .block()!!

    init {
        createScheduledTask()
    }

    // TODO: Localize this
    private fun createScheduledTask() {
        Flux.interval(Duration.ofSeconds(5))
                .flatMap { _ ->
                    database.get("select * from contests where completed=? and unix_timestamp() > votingEndTime", false)
                            .map(Row::columns)
                            .flatMap { columns ->
                                shards[0].getChannelById(Snowflake.of(columns["submissionChannelId"] as Long))
                                        .ofType(GuildMessageChannel::class.java)
                                        .flatMap { channel ->
                                            if (columns["winnerId"] != null)
                                                shards[0].getUserById(Snowflake.of(columns["winnerId"] as Long))
                                                        .flatMap { channel.createMessage("Your contest is over punk!\nThe winner is ${it.mention}") }
                                            else
                                                channel.createMessage("The contest finished but nobody won :(")
                                        }
                                        .flatMap { database.set("update contests set completed=true where id=? and name=?", columns["id"] as Long, columns["name"]) }
                            }
                }
                .subscribe()
        logger.info("Created scheduled time task")
    }

    /**
     * Logs all of the shards in and set the event listeners
     */
    fun login() {
        Mono.`when`(
                shards.map { shard ->
                    shard.login().and(shard.eventDispatcher
                            .on(MessageCreateEvent::class.java)
                            .filterWhen { it.message.channel.map { c -> c.type == Channel.Type.GUILD_TEXT } }
                            .filter { it.member.isPresent }
                            .filter { !it.member.get().isBot }
                            .filter { it.message.content.isPresent }
                            .flatMap { eventHandler.onMessageCreateEvent(it) }
                    ).onErrorContinue { t, u -> logger.error(t.message) }
                }
        )
                .block()
    }

    /**
     * Sets the presence for all of the shards
     */
    fun updatePresence(presence: Presence) {
        shards.forEach { it.updatePresence(presence).block() }
    }
}