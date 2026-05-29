package com.nextersolutions.soulmetric.ui.util

import com.nextersolutions.soulmetric.core.domain.model.Survey

/**
 * Maps a [Survey] to an appropriate emoji icon.
 *
 * Strategy (in priority order):
 *  1. Exact survey-ID lookup — guarantees a curated icon for every known survey.
 *  2. Keyword match against the words in the survey ID (split on `_`/`-`/` `).
 *  3. Keyword match against the words in the survey title.
 *  4. Deterministic fallback from the combined pool using the survey-ID hash.
 *
 * Icon catalogue — 6 categories × 8 icons = 48 total.
 */
object SurveyIconProvider {

    // ── Category pools ────────────────────────────────────────────────────────

    /** Stress, anxiety, mental load */
    private val stressIcons = listOf(
        "🌊", "⚡", "🧠", "🌀", "💭", "🌧️", "🔥", "😣"
    )

    /** Sleep, rest, recovery, breathing */
    private val restIcons = listOf(
        "🌙", "💤", "🔋", "🍃", "🧘", "🕊️", "🌌", "✨"
    )

    /** Wellbeing, health, happiness, mood */
    private val wellbeingIcons = listOf(
        "🌱", "☀️", "💚", "🌸", "🌿", "🤗", "😊", "🌈"
    )

    /** Relationships, family, love, social connection */
    private val relationshipIcons = listOf(
        "💝", "🏠", "❤️", "🤝", "💬", "👐", "💑", "👪"
    )

    /** Personality, identity, character, self-awareness */
    private val personalityIcons = listOf(
        "🎭", "🔮", "🦋", "💎", "🧩", "🎨", "🔑", "🌀"
    )

    /** Motivation, achievement, goals, success */
    private val motivationIcons = listOf(
        "🚀", "🏆", "⭐", "💡", "📈", "🎯", "🏅", "🌟"
    )

    private val allIcons: List<String> =
        stressIcons + restIcons + wellbeingIcons +
        relationshipIcons + personalityIcons + motivationIcons

    // ── Exact ID → icon (curated, one per known survey) ──────────────────────

    private val exactMap: Map<String, String> = mapOf(
        "stress_assessment"            to "🌊",
        "wellbeing_check"              to "🌱",
        "family_happiness_assessment"  to "🏠",
        "temperament_type"             to "🎭",
        "motivation_achievement"       to "🚀"
    )

    // ── Keyword → category (ordered; first match wins) ────────────────────────

    private val keywordCategories: List<Pair<Set<String>, List<String>>> = listOf(
        setOf(
            "stress", "anxiety", "burnout", "tension",
            "overwhelm", "pressure", "irritab"
        ) to stressIcons,

        setOf(
            "sleep", "rest", "fatigue", "energy",
            "breathing", "calm", "mindful", "relax", "recovery"
        ) to restIcons,

        setOf(
            "wellbeing", "well", "health", "happiness",
            "mood", "joy", "emotion", "satisfaction"
        ) to wellbeingIcons,

        setOf(
            "relationship", "family", "love", "partner",
            "marriage", "social", "trust", "connection",
            "communication", "intimacy", "friendship"
        ) to relationshipIcons,

        setOf(
            "personality", "temperament", "character",
            "type", "introvert", "extrovert", "ambivert",
            "identity", "self", "trait", "behavior"
        ) to personalityIcons,

        setOf(
            "motivation", "achievement", "goal", "success",
            "career", "performance", "ambition", "drive",
            "growth", "progress", "purpose", "potential"
        ) to motivationIcons
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns the emoji icon for [survey]. Always returns a non-empty string. */
    fun iconFor(survey: Survey): String {
        // 1. Exact match
        exactMap[survey.id]?.let { return it }

        // 2. Keyword match in survey ID
        val idWords = survey.id.lowercase().split("_", "-", " ").toSet()
        pickFromKeywords(idWords)?.let { return it }

        // 3. Keyword match in survey title
        val titleWords = survey.title.lowercase()
            .split(" ", "_", "-", ",", ".", ":", "!")
            .filter { it.length > 2 }
            .toSet()
        pickFromKeywords(titleWords)?.let { return it }

        // 4. Deterministic fallback
        return allIcons[hash(survey.id) % allIcons.size]
    }

    private fun pickFromKeywords(words: Set<String>): String? {
        for ((keywords, icons) in keywordCategories) {
            if (keywords.any { kw -> words.any { w -> w.contains(kw) } }) {
                return icons[hash(words.minOrNull() ?: "") % icons.size]
            }
        }
        return null
    }

    /** Stable, non-negative hash of a string. */
    private fun hash(s: String): Int = (s.hashCode() and Int.MAX_VALUE).coerceAtLeast(1)
}
