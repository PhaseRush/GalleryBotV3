package com.phaserush.gallerybot.data.argument

class WordArgument : Argument<String>(
        "user-input",
        "user-input-desc"
) {
    override fun parse(input: String) {
        value = input
    }
}