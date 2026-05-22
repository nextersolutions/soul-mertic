package com.nextersolutions.soulmetric.core.data.repository

import android.content.Context
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyCacheDao
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyCacheEntity
import com.nextersolutions.soulmetric.core.data.remote.api.SurveyApiService
import com.nextersolutions.soulmetric.core.data.remote.dto.SurveyDto
import com.nextersolutions.soulmetric.core.data.remote.dto.SurveysResponseDto
import com.nextersolutions.soulmetric.core.data.repository.SurveyMapper.toDomain
import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.core.domain.repository.SurveyRepository
import com.nextersolutions.soulmetric.core.util.LocaleResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: SurveyCacheDao,
    private val api: SurveyApiService,
    private val json: Json
) : SurveyRepository {

    override fun getSurveys(): Flow<List<Survey>> {
        val locale = LocaleResolver.resolve(context)
        return dao.observeAll()
            .onStart { seedFromAssetsIfEmpty() }
            .map { entities ->
                entities.map { entity ->
                    json.decodeFromString<SurveyDto>(entity.json).toDomain(locale)
                }
            }
    }

    override suspend fun getSurveyById(id: String): Survey? {
        val locale = LocaleResolver.resolve(context)
        seedFromAssetsIfEmpty()
        return dao.getById(id)?.let { entity ->
            json.decodeFromString<SurveyDto>(entity.json).toDomain(locale)
        }
    }

    override suspend fun refreshSurveysFromNetwork(): Result<Unit> = runCatching {
        val response = api.fetchSurveys()
        persistDtos(response.surveys)
    }

    // ---- helpers ----

    private suspend fun seedFromAssetsIfEmpty() {
        if (dao.count() == 0) {
            // table is empty – seed from bundled assets
            val text = context.assets.open("surveys.json").bufferedReader().readText()
            val response = json.decodeFromString<SurveysResponseDto>(text)
            persistDtos(response.surveys)
        }
    }

    private suspend fun persistDtos(dtos: List<SurveyDto>) {
        val entities = dtos.map { dto ->
            SurveyCacheEntity(
                id = dto.id,
                version = dto.version,
                json = json.encodeToString(dto)
            )
        }
        dao.upsertAll(entities)
    }
}
