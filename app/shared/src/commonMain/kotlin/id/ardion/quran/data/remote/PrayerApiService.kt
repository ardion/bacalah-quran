package id.ardion.quran.data.remote

import id.ardion.quran.config.ApiConfig
import id.ardion.quran.data.response.AladhanResponseDto
import id.ardion.quran.data.response.GeocodeResponseDto
import id.ardion.quran.data.response.NominatimResponseDto
import id.ardion.quran.domain.model.LocationCoordinates
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

private const val NOMINATIM_STREET_ZOOM = 18
private const val GEOCODE_LANGUAGE = "id"

class PrayerApiService(private val httpClient: HttpClient) {

    suspend fun fetchPrayerTimings(location: LocationCoordinates): AladhanResponseDto =
        httpClient.get(ApiConfig.PRAYER_TIMES_URL) {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("method", ApiConfig.PRAYER_CALCULATION_METHOD)
        }.body()

    suspend fun fetchReverseGeocodeNominatim(location: LocationCoordinates): NominatimResponseDto =
        httpClient.get(ApiConfig.NOMINATIM_REVERSE_URL) {
            header(HttpHeaders.UserAgent, ApiConfig.NOMINATIM_USER_AGENT)
            parameter("format", "json")
            parameter("lat", location.latitude)
            parameter("lon", location.longitude)
            parameter("zoom", NOMINATIM_STREET_ZOOM)
            parameter("addressdetails", 1)
        }.body()

    suspend fun fetchReverseGeocodeBigData(location: LocationCoordinates): GeocodeResponseDto =
        httpClient.get(ApiConfig.BIGDATACLOUD_REVERSE_URL) {
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("localityLanguage", GEOCODE_LANGUAGE)
        }.body()
}
