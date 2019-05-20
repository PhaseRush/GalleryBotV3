package com.phaserush.gallerybot

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class ShardManager {
    val shards: List<DiscordClient>

    init {
        val tempShards: MutableList<DiscordClient> = mutableListOf()
        val shardCount = if (config.debug) 1 else config.shardCount

        for (i: Int in 0 until shardCount) {
            tempShards.add(DiscordClientBuilder(if (config.debug) config.debugToken else config.token)
                    .setShardCount(shardCount)
                    .setShardIndex(i)
                    .setInitialPresence(Presence.idle(Activity.playing(config.presenceMessage)))
                    .setEventScheduler(Schedulers.immediate())
                    .build())
        }
        shards = tempShards
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
                            .flatMap { it.message.channel.flatMap { c -> c.createMessage("hoi") } }
                    ).onErrorContinue { t, u -> t.printStackTrace() }
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