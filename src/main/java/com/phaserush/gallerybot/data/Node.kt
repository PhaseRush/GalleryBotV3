package com.phaserush.gallerybot.data

class Node<T>(
        val data: T,
        val children: List<Node<T>>
) {
    var parent: Node<T>? = null

    init {
        children.forEach { it.parent = this }
    }
}