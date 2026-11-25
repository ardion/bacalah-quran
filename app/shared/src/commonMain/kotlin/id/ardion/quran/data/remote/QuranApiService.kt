package id.ardion.quran.data.remote

import id.ardion.quran.config.ApiConfig
import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.PageDataDto
import id.ardion.quran.data.response.SurahListItemDto
import id.ardion.quran.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class QuranApiService(private val httpClient: HttpClient) {

    suspend fun getAllSurahs(): List<SurahListItemDto>? =
        httpClient.get("surah").body<ApiResponse<List<SurahListItemDto>>>().data

    suspend fun getSurahDetailMultiEdition(surahNumber: Int): List<MultiEditionSurahDto>? =
        httpClient.get("surah/$surahNumber/editions/${ApiConfig.SURAH_DETAIL_EDITIONS}")
            .body<ApiResponse<List<MultiEditionSurahDto>>>().data

    suspend fun getPageTajweed(pageNumber: Int): PageDataDto? =
        httpClient.get("page/$pageNumber/${ApiConfig.TAJWEED_EDITION}")
            .body<ApiResponse<PageDataDto>>().data
}
