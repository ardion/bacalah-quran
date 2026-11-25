package id.ardion.quran.presentation.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.usecase.GetPrayerTimesUseCase
import id.ardion.quran.platform.CompassSensor
import id.ardion.quran.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val COMPASS_STOP_TIMEOUT_MS = 5_000L

class PrayerViewModel(
    private val getPrayerTimes: GetPrayerTimesUseCase,
    private val compassSensor: CompassSensor
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<PrayerTimes>>(UiState.Loading)
    val uiState: StateFlow<UiState<PrayerTimes>> = _uiState.asStateFlow()

    val compass: StateFlow<CompassReading> = combine(
        compassSensor.azimuth,
        compassSensor.accuracy,
        compassSensor.isFlat
    ) { azimuth, accuracy, isFlat ->
        CompassReading(azimuth, accuracy, isFlat, compassSensor.isAvailable)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(COMPASS_STOP_TIMEOUT_MS),
        CompassReading(
            azimuth = compassSensor.azimuth.value,
            accuracy = compassSensor.accuracy.value,
            isFlat = compassSensor.isFlat.value,
            isAvailable = compassSensor.isAvailable
        )
    )

    private var loadJob: Job? = null

    fun loadPrayerTimes() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            getPrayerTimes()
                .onSuccess { times ->
                    compassSensor.setLocation(times.latitude, times.longitude)
                    _uiState.value = UiState.Success(times)
                }
                .onFailure { _uiState.value = UiState.Error(it) }
        }
    }

    fun startCompass() = compassSensor.start()

    fun stopCompass() = compassSensor.stop()

    override fun onCleared() {
        super.onCleared()
        compassSensor.stop()
    }
}
