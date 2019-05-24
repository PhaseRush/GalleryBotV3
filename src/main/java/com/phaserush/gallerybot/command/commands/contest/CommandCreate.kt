package com.phaserush.gallerybot.command.commands.contest

import com.phaserush.gallerybot.command.Command
import com.phaserush.gallerybot.command.CommandContext
import com.phaserush.gallerybot.data.argument.ChannelArgument
import com.phaserush.gallerybot.data.argument.WordArgument
import reactor.core.publisher.Mono

class CommandCreate : Command(
        "create",
        "create-help",
        listOf(WordArgument(), ChannelArgument(), ChannelArgument(), ChannelArgument(), ChannelArgument()/*,
                TimeArgument(), TimeArgument(), TimeArgument(), TimeArgument(), TimeArgument(), TimeArgument()*/)
) {
    // TODO: Localize this im sorry
    override fun call(context: CommandContext): Mono<Void> {
        return Mono.empty()

        /*val contestName = arguments[0].value as String
        val contestTheme = arguments[1].value as String?

        return context.event.guild
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
                }.then()*/

        /*val newContest = Contest.create(
                context.event.guildId.get(),
                contestName,
                contestTheme,
                context.event.guild.flatMap { it.channels.filter this shit i guess }

                )
        return Mono.empty()*/
    }
}