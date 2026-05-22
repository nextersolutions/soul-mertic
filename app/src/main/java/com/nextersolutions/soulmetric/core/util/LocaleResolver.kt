package com.nextersolutions.soulmetric.core.util

import android.content.Context
import java.util.Locale

object LocaleResolver {
    private val supportedLocales = setOf("en", "de", "ua")
    private const val FALLBACK = "en"

    fun resolve(context: Context): String {
        val tag = context.resources.configuration.locales[0].language
        // Ukrainian system locale is "uk", map to our JSON key "ua"
        val mapped = if (tag == "uk") "ua" else tag
        return if (mapped in supportedLocales) mapped else FALLBACK
    }

    fun Map<String, String>.localized(locale: String): String =
        this[locale] ?: this[FALLBACK] ?: values.firstOrNull() ?: ""
}
