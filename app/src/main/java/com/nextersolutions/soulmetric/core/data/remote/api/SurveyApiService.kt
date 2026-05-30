package com.nextersolutions.soulmetric.core.data.remote.api

import com.nextersolutions.soulmetric.core.data.remote.dto.SurveysResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class SurveyApiService(private val client: HttpClient) {
    suspend fun fetchSurveys(): SurveysResponseDto =
        client.get("surveys.json").body()
}
