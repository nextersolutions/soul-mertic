package com.nextersolutions.soulmetric.core.domain.model

data class Survey(
    val id: String,
    val version: Int,
    val title: String,
    val description: String,
    val questions: List<Question>,
    val sections: List<Section> = emptyList(),
    val scoring: Scoring?
)

data class Section(
    val id: String,
    val title: String
)

sealed class Question {
    abstract val id: String
    abstract val text: String
    abstract val required: Boolean
    abstract val sectionId: String?

    data class Scale(
        override val id: String,
        override val text: String,
        override val required: Boolean,
        override val sectionId: String? = null,
        val min: Int,
        val max: Int,
        val minLabel: String,
        val maxLabel: String
    ) : Question()

    data class Choice(
        override val id: String,
        override val text: String,
        override val required: Boolean,
        override val sectionId: String? = null,
        val options: List<ChoiceOption>
    ) : Question()

    data class TextInput(
        override val id: String,
        override val text: String,
        override val required: Boolean,
        override val sectionId: String? = null
    ) : Question()
}

data class ChoiceOption(
    val id: String,
    val text: String,
    val value: Int = 0
)

data class Scoring(
    val method: String,
    val scoredQuestions: List<String> = emptyList(),
    val ranges: List<ScoreRange>
)

data class ScoreRange(
    val min: Int,
    val max: Int,
    val label: String = "",
    val description: String
)
