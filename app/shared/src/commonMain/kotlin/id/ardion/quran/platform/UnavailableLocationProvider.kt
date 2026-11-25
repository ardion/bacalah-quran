package id.ardion.quran.platform

import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.repository.LocationProvider

class UnavailableLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): LocationResult = LocationResult.Unavailable
}
