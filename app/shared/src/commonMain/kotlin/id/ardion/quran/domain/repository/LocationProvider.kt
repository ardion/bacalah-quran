package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.LocationResult

interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}
