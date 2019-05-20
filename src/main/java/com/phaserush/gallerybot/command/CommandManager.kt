package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.command.commands.CommandPing
import com.phaserush.gallerybot.data.Node

class CommandManager {
    val commands: Map<String, Command>
    val commandNodes: List<Node<Command>> = listOf(
            Node<Command>(CommandPing(), emptyList())
    )

    init {
        val temp: MutableMap<String, Command> = mutableMapOf()
        commandNodes.forEach {
            temp[it.data.name] = it.data
            it.data.aliases.forEach { alias -> temp[alias] = it.data }
        }
        commands = temp
    }
}