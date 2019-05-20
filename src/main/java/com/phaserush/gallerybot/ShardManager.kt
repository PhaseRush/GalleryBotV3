package com.phaserush.gallerybot

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.presence.Activity
import discord4j.core.`object`.presence.Presence
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.shard.ShardingClientBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

class ShardManager {
    val eventHandler = EventHandler()
    val shards: List<DiscordClient> = ShardingClientBuilder(config.token)
            .setShardCount(1)
            .build()
            .map { it.build() }
            .collectList()
            .block()!!

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