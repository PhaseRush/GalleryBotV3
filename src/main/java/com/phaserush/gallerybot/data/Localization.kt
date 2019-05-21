package com.phaserush.gallerybot.data

import java.io.File
import java.util.*

class Localization {
    private val locales: Set<Locale> = File(Localization::class.java.classLoader.getResource("locale/").path)
            .listFiles()
            .map { Locale.forLanguageTag(it.name.substring(7, 12).replace('_', '-')) }
            .toSet()

    /**
     * Get the localized message for a key
     *
     * @param locale The locale to get the message in
     * @param key The key of the message in the resource bundle
     * @param parameters The parameters to fill into the message
     * @return The localized message properly formatted and ready to use
     */
    fun getMessage(locale: Locale, key: String, vararg parameters: Any): String {
        return String.format(ResourceBundle.getBundle("locale/locale", locale).getString(key), *parameters)
    }
}