package com.nextersolutions.soulmetric.core.domain.model

data class Survey(
    val id: String,
    val version: Int,
    val title: String,
    val description: String,
    val questions: List<Question>,
    val scoring: Scoring?
)

sealed class Question {
    abstract val id: String
    abstract val text: String
    abstract val required: Boolean

    data class Scale(
        override val id: String,
        override val text: String,
        override val required: Boolean,
        val min: Int,
        val max: Int,
        val minLabel: String,
        val maxLabel: String
    ) : Question()

    data class Choice(
        override val id: String,
        override val text: String,
        override val required: Boolean,
        val options: List<ChoiceOption>
    ) : Question()

    data class TextInput(
        override val id: String,
        override val text: String,
        override val required: Boolean
    ) : Question()
}

data class ChoiceOption(
    val id: String,
    val text: String
)

data class Scoring(
    val method: String,
    val ranges: List<ScoreRange>
)

data class ScoreRange(
    val min: Int,
    val max: Int,
    val description: String
)
