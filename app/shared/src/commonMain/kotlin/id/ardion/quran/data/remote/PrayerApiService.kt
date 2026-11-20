package id.ardion.quran.data.remote

import id.ardion.quran.data.response.AladhanResponseDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.NominatimResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

class PrayerApiService(
    private val httpClient: HttpClient
) {
    suspend fun fetchPrayerTimings(latitude: Double, longitude: Double): AladhanResponseDto {
        return httpClient.get("https://api.aladhan.com/v1/timings?latitude=$latitude&longitude=$longitude&method=20").body()
    }

    suspend fun fetchReverseGeocodeNominatim(latitude: Double, longitude: Double): NominatimResponseDto {
        return httpClient.get("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=18&addressdetails=1") {
            header("User-Agent", "BacalahQuranApp/1.0")
        }.body()
    }

    suspend fun fetchReverseGeocodeBigData(latitude: Double, longitude: Double): GeocodeResponseDto {
        return httpClient.get("https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=id").body()
    }
}
