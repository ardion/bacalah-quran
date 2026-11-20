package id.ardion.quran.presentation.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.usecase.GetPrayerTimesUseCase
import id.ardion.quran.state.UiState
import id.ardion.quran.utils.CompassSensor
import id.ardion.quran.utils.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrayerViewModel(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val locationService: LocationService,
    val compassSensor: CompassSensor
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<PrayerTimes>>(UiState.Loading)
    val uiState: StateFlow<UiState<PrayerTimes>> = _uiState

    fun loadPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val location = locationService.getCurrentLocation()
            val lat = location?.latitude ?: 0.0
            val lon = location?.longitude ?: 0.0

            getPrayerTimesUseCase(lat, lon)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Gagal memuat jadwal sholat") }
        }
    }

    fun startCompass() {
        compassSensor.start()
    }

    fun stopCompass() {
        compassSensor.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopCompass()
    }
}
