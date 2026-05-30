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
            .onStart { syncNewSurveysFromAssets() }
            .map { entities ->
                entities.map { entity ->
                    json.decodeFromString<SurveyDto>(entity.json).toDomain(locale)
                }
            }
    }

    override suspend fun getSurveyById(id: String): Survey? {
        val locale = LocaleResolver.resolve(context)
        syncNewSurveysFromAssets()
        return dao.getById(id)?.let { entity ->
            json.decodeFromString<SurveyDto>(entity.json).toDomain(locale)
        }
    }

    /**
     * Downloads surveys from the network and persists any that are new or have a
     * higher version than what is currently cached. Existing surveys with an equal
     * or higher cached version are left untouched.
     */
    override suspend fun refreshSurveysFromNetwork(): Result<Unit> = runCatching {
        val response = api.fetchSurveys()
        persistVersionAware(response.surveys)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Reads the bundled assets file and inserts/updates any surveys that are either
     * absent from the cache or have a higher version than the cached copy.
     */
    private suspend fun syncNewSurveysFromAssets() {
        val text = context.assets.open("surveys.json").bufferedReader().readText()
        val response = json.decodeFromString<SurveysResponseDto>(text)
        persistVersionAware(response.surveys)
    }

    /**
     * Upserts only the surveys whose version is strictly higher than what is cached,
     * plus any survey IDs not yet present in the cache.
     */
    private suspend fun persistVersionAware(dtos: List<SurveyDto>) {
        val cachedVersions: Map<String, Int> = dao.getAll().associate { it.id to it.version }

        val toUpsert = dtos.filter { dto ->
            val cached = cachedVersions[dto.id]
            cached == null || dto.version > cached
        }

        if (toUpsert.isNotEmpty()) {
            dao.upsertAll(toUpsert.map { dto ->
                SurveyCacheEntity(
                    id = dto.id,
                    version = dto.version,
                    json = json.encodeToString(dto)
                )
            })
        }
    }
}
