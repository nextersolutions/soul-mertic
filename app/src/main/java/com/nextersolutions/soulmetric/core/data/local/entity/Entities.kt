package com.nextersolutions.soulmetric.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "survey_cache")
data class SurveyCacheEntity(
    @PrimaryKey val id: String,
    val version: Int,
    val json: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "survey_results")
data class SurveyResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surveyId: String,
    val surveyTitle: String,
    val completedAt: Long,
    val score: Int?,
    val scoreDescription: String?
)

@Entity(
    tableName = "survey_answers",
    foreignKeys = [ForeignKey(
        entity = SurveyResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["resultId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("resultId")]
)
data class SurveyAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resultId: Long,
    val questionId: String,
    val type: String,         // "scale" | "choice" | "text"
    val intValue: Int?,
    val stringValue: String?
)
