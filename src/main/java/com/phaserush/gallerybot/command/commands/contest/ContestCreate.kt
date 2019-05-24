package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.WordArgument
import com.phaserush.gallerybot.data.contest.Contest
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.CategoryCreateSpec
import discord4j.core.spec.TextChannelCreateSpec
import reactor.core.publisher.Mono
import java.util.function.Consumer

class ContestCreate : Command(
        "create",
        "create-help",
        listOf(WordArgument())
) {
    // TODO: Localize this im sorry
    override fun call(context: CommandContext): Mono<Void> {
        val contestName = arguments[0].value as String
        val contestTheme = arguments[1].value as String?

        context.event.guild
                .flatMap {
                    it.createCategory(categorySpec("Contest-$contestName", 2))
                            .flatMap { categorySpec ->
                                context.event.guild
                                        .flatMap { guild -> guild.createTextChannel(channelSpec("submissions", false, categorySpec.id, 0)) }
                                        .flatMap { tc -> tc.guild }
                                        .flatMap { guild -> guild.createTextChannel(channelSpec("nsfw-submissions", true, categorySpec.id, 1)) }
                                        .flatMap { tc -> tc.guild }
                                        .flatMap { guild -> guild.createTextChannel(channelSpec("voting", false, categorySpec.id, 2)) }
                                        .flatMap { tc -> tc.guild }
                                        .flatMap { guild -> guild.createTextChannel(channelSpec("nsfw-voting", true, categorySpec.id, 3)) }
                            }
                }


        val newContest = Contest.create(
                context.event.guildId.get(),
                contestName,
                contestTheme,
                context.event.guild.flatMap { it.channels.filter this shit i guess }

                )
        return Mono.empty()
    }

    private fun categorySpec(name: String, rank: Int = 2): Consumer<CategoryCreateSpec> {
        return Consumer { it.setName(name).setPosition(rank) }
    }

    private fun channelSpec(name: String, isNsfw: Boolean, categoryId: Snowflake, rank: Int): Consumer<TextChannelCreateSpec> {
        return Consumer { it.setName(name).setNsfw(isNsfw).setParentId(categoryId).setPosition(rank).setPermissionOverwrites() }
    }
}