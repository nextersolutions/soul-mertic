package com.nextersolutions.soulmetric.core.domain.model

import java.time.Instant

data class SurveyResult(
    val id: Long = 0,
    val surveyId: String,
    val surveyTitle: String,
    val completedAt: Instant,
    val answers: List<Answer>,
    val score: Int?,
    val scoreDescription: String?
)

sealed class Answer {
    abstract val questionId: String

    data class ScaleAnswer(
        override val questionId: String,
        val value: Int
    ) : Answer()

    data class ChoiceAnswer(
        override val questionId: String,
        val selectedOptionId: String,
        val selectedOptionText: String
    ) : Answer()

    data class TextAnswer(
        override val questionId: String,
        val value: String
    ) : Answer()
}
