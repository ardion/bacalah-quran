package id.ardion.quran.data.remote

import id.ardion.quran.config.ApiConfig
import id.ardion.quran.data.response.KemenagCityDto
import id.ardion.quran.data.response.KemenagScheduleDto
import id.ardion.quran.data.response.KemenagCitySearchDto
import id.ardion.quran.data.response.KemenagScheduleResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLPathPart

class KemenagApiService(private val httpClient: HttpClient) {

    suspend fun searchCity(keyword: String): List<KemenagCityDto> =
        httpClient.get("${ApiConfig.KEMENAG_SCHEDULE_URL}/kota/cari/${keyword.encodeURLPathPart()}")
            .body<KemenagCitySearchDto>()
            .data

    suspend fun monthlySchedule(cityId: String, year: Int, month: Int): KemenagScheduleDto? =
        httpClient.get("${ApiConfig.KEMENAG_SCHEDULE_URL}/jadwal/$cityId/$year/$month")
            .body<KemenagScheduleResponseDto>()
            .data
}
