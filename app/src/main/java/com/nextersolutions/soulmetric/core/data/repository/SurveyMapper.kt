package com.nextersolutions.soulmetric.core.data.repository

import com.nextersolutions.soulmetric.core.data.remote.dto.*
import com.nextersolutions.soulmetric.core.domain.model.*
import com.nextersolutions.soulmetric.core.util.LocaleResolver.localized

object SurveyMapper {

    fun SurveyDto.toDomain(locale: String): Survey = Survey(
        id = id,
        version = version,
        title = title.localized(locale),
        description = description.localized(locale),
        questions = questions.mapNotNull { it.toDomain(locale) },
        scoring = scoring?.toDomain(locale)
    )

    private fun QuestionDto.toDomain(locale: String): Question? = when (type) {
        "scale" -> {
            val s = scale ?: return null
            Question.Scale(
                id = id,
                text = text.localized(locale),
                required = required,
                min = s.min,
                max = s.max,
                minLabel = s.labels[s.min.toString()]?.localized(locale) ?: s.min.toString(),
                maxLabel = s.labels[s.max.toString()]?.localized(locale) ?: s.max.toString()
            )
        }
        "choice" -> Question.Choice(
            id = id,
            text = text.localized(locale),
            required = required,
            options = (options ?: emptyList()).map { opt ->
                ChoiceOption(id = opt.id, text = opt.text.localized(locale))
            }
        )
        "text" -> Question.TextInput(
            id = id,
            text = text.localized(locale),
            required = required
        )
        else -> null
    }

    private fun ScoringDto.toDomain(locale: String) = Scoring(
        method = method,
        ranges = ranges.map { r ->
            ScoreRange(min = r.min, max = r.max, description = r.description.localized(locale))
        }
    )
}
