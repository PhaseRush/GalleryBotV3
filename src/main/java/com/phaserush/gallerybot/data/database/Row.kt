package com.phaserush.gallerybot.data.database

class Row {
    val columns: HashMap<String, Any> = hashMapOf()

    fun addColumn(key: String, data: Any) = columns.put(key, data)
}