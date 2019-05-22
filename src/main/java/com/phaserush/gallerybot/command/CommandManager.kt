package com.phaserush.gallerybot.command

import com.phaserush.gallerybot.command.commands.CommandContest
import com.phaserush.gallerybot.command.commands.CommandPing
import com.phaserush.gallerybot.command.commands.CommandSubmit
import com.phaserush.gallerybot.data.Node
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

class CommandManager {
    val commands: Map<String, Command>
    val commandNodes: List<Node<Command>> = listOf(
            Node<Command>(CommandPing()),
            Node(CommandContest(),
                    listOf(Node<Command>(CommandSubmit())))
    )

    init {
        val temp: MutableMap<String, Command> = mutableMapOf()
        commandNodes.forEach {
            temp[it.data.name] = it.data
            it.data.aliases.forEach { alias -> temp[alias] = it.data }
        }
        commands = temp
    }

    fun traverseThis(list: List<String>): Tuple2<Command?, List<String>> {
        var nextChildren: List<Node<Command>> = commandNodes // track frontier
        var command: Command? = null // may or may not contain a command dont flame i use var

        var idx = 0
        for (i in 0 until list.size) {
            val word = list[i]
            val nextCmd = checkChildren(nextChildren, word)
            if (nextCmd == null) {
                return Tuples.of(command, getArgs(i, list))
            } else {
                command = nextCmd
                nextChildren = nextChildren.flatMap { it.children }.toMutableList()
                idx++
            }
        }

        return Tuples.of(command, getArgs(idx, list)) // should never run here, but we'll see :)
    }

    private fun getArgs(idx: Int, list: List<String>): List<String> = if (idx < list.size) list.subList(idx, list.size) else emptyList()


    private fun checkChildren(nodes: List<Node<Command>>, query: String): Command? {
        return nodes.stream()
                .filter { it.data.aliases.contains(query) || it.data.name == query }
                .findFirst()
                .map { it.data }
                .orElse(null)
    }
}