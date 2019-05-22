package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.data.argument.Argument
import discord4j.core.`object`.util.Permission
import discord4j.core.`object`.util.PermissionSet
import reactor.core.publisher.Mono

abstract class Command(
        /**
         * Name of the command
         */
        val name: String,

        /**
         * The help message key in the localization file for the necessary locale
         */
        val help: String,

        /**
         * The list of arguments that the command requires
         */
        val arguments: List<Argument<*>> = emptyList(),

        /**
         * The permission command for this command, checks bot & user permissions
         * before execution of the command
         * @see PermissionCheck
         */
        val permissions: PermissionCheck = PermissionCheck(PermissionSet.of(Permission.SEND_MESSAGES), PermissionSet.of(Permission.SEND_MESSAGES)),

        /**
         * Aliases for the command
         */
        val aliases: Set<String> = emptySet()
) {
    /**
     * Execute the command, *this does not do permission checking*
     */
    abstract fun call(context: CommandContext): Mono<Void>
}