package id.ardion.quran.data.remote

import id.ardion.quran.data.response.MultiEditionSurahDto
import id.ardion.quran.data.response.SurahDataDto
import id.ardion.quran.data.response.SurahListItemDto
import id.ardion.quran.network.ApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class QuranApiService(private val httpClient: HttpClient) {

    suspend fun getSurahTajweed(surahNumber: Int): SurahDataDto {
        val response: ApiResponse<SurahDataDto> = httpClient
            .get("surah/$surahNumber/quran-tajweed")
            .body()
        return response.data
    }

    suspend fun getAllSurahs(): List<SurahListItemDto> {
        val response: ApiResponse<List<SurahListItemDto>> = httpClient
            .get("surah")
            .body()
        return response.data
    }

    suspend fun getSurahDetailMultiEdition(surahNumber: Int): List<MultiEditionSurahDto> {
        val response: ApiResponse<List<MultiEditionSurahDto>> = httpClient
            .get("surah/$surahNumber/editions/quran-tajweed,id.indonesian,en.transliteration,ar.alafasy")
            .body()
        return response.data
    }
}
