package com.phaserush.gallerybot

data class Config(
        val token           : String,
        val prefix          : String,
        val presenceMessage : String,
        val databaseIp      : String,
        val databasePort    : String,
        val databaseName    : String,
        val databaseUsername: String,
        val databasePassword: String
)