package com.phaserush.gallerybot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.phaserush.gallerybot.data.database.Database
import java.io.FileInputStream

val config: Config = ObjectMapper().registerModule(KotlinModule()).readValue(FileInputStream("./data/launch.json"), Config::class.java)
val database: Database = Database()

fun main() {
    val shardManager = ShardManager()

    shardManager.login()
}