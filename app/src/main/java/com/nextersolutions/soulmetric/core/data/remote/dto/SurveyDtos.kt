package com.nextersolutions.soulmetric.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SurveysResponseDto(
    val surveys: List<SurveyDto>
)

@Serializable
data class SurveyDto(
    val id: String,
    val version: Int,
    val title: Map<String, String>,
    val description: Map<String, String> = emptyMap(),
    val questions: List<QuestionDto>,
    val sections: List<SectionDto> = emptyList(),
    val scoring: ScoringDto? = null
)

@Serializable
data class SectionDto(
    val id: String,
    val title: Map<String, String>
)

@Serializable
data class QuestionDto(
    val id: String,
    val type: String,
    val text: Map<String, String>,
    val required: Boolean = true,
    val section: String? = null,
    val scale: ScaleDto? = null,
    val options: List<OptionDto>? = null
)

@Serializable
data class ScaleDto(
    val min: Int,
    val max: Int,
    val labels: Map<String, Map<String, String>> = emptyMap()
)

@Serializable
data class OptionDto(
    val id: String,
    val text: Map<String, String>,
    val value: Int = 0
)

@Serializable
data class ScoringDto(
    val method: String,
    @SerialName("scored_questions") val scoredQuestions: List<String> = emptyList(),
    val ranges: List<ScoreRangeDto>
)

@Serializable
data class ScoreRangeDto(
    val min: Int,
    val max: Int,
    val label: Map<String, String> = emptyMap(),
    val description: Map<String, String>
)
