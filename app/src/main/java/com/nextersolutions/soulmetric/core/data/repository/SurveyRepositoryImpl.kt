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

    override suspend fun refreshSurveysFromNetwork(): Result<Unit> = runCatching {
        val response = api.fetchSurveys()
        persistDtos(response.surveys)
    }

    // ---- helpers ----

    /**
     * Called on every startup. Ensures the cache is in sync with the bundled assets:
     * - Surveys not yet in the DB are inserted.
     * - Surveys whose assets version is higher than the cached version are upserted
     *   (this covers the case where surveys.json is updated between app installs).
     * - Surveys where the cached version is already equal or higher (e.g. from a network
     *   refresh) are left untouched.
     */
    private suspend fun syncNewSurveysFromAssets() {
        val text = context.assets.open("surveys.json").bufferedReader().readText()
        val response = json.decodeFromString<SurveysResponseDto>(text)

        val cachedVersions: Map<String, Int> =
            dao.getAllVersions().associate { it.id to it.version }

        // Include a survey if it is new (not in DB) or if the assets version is higher
        // than what's cached (covers surveys.json updates between installs).
        val staleOrMissing = response.surveys.filter { dto ->
            val cached = cachedVersions[dto.id]
            cached == null || dto.version > cached
        }

        if (staleOrMissing.isNotEmpty()) {
            persistDtos(staleOrMissing)
        }
    }

    /** Used by network refresh — replaces cached surveys with the latest server versions. */
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
