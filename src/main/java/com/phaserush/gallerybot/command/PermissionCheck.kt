package com.phaserush.gallerybot.command

import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.util.Permission
import discord4j.core.event.domain.message.MessageCreateEvent
import reactor.core.publisher.Mono

class PermissionCheck(
        /**
         * The permissions required by the bot to execute
         * the command, e.g. SEND_MESSAGES or KICK_MEMBER
         */
        val botPermissions: Set<Permission>,

        /**
         * The permissions required by the user to execute
         * the command, same as above
         */
        val userPermissions: Set<Permission>
) {
    /**
     * Test a member to determine whether or not the member
     * has sufficient permissions to run the command
     *
     * @param event The event context
     * @return A set returning all the missing permissions,
     * if the set is empty all the permissions are present
     */
    fun testMember(event: MessageCreateEvent): Mono<Set<Permission>> {
        return event.message.channel.ofType(TextChannel::class.java)
                .flatMap { c ->
                    c.getEffectivePermissions(event.member.get().id).map { set ->
                        userPermissions.subtract(set.toList())
                    }
                }
    }

    /**
     * Test whether the bot has all the permissions required
     * to execute the command
     *
     * @param event The event context
     * @return A set returning all the missing permissions,
     * if the set is empty all the permissions are present
     */
    fun testBot(event: MessageCreateEvent): Mono<Set<Permission>> {
        return event.message.channel.ofType(TextChannel::class.java)
                .flatMap { c ->
                    c.getEffectivePermissions(event.client.selfId.get()).map { set ->
                        botPermissions.subtract(set.toList())
                    }
                }
    }
}