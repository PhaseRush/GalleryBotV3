package com.phaserush.gallerybot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.FileInputStream

val config: Config = ObjectMapper().registerModule(KotlinModule()).readValue(FileInputStream("./data/launch.json"), Config::class.java)

fun main() {
    val shardManager = ShardManager()

    shardManager.login()
}