package id.ardion.quran.presentation.surah

import id.ardion.quran.audio.AudioPlayer
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SurahDetailViewModel(
    private val getSurahDetailUseCase: GetSurahDetailUseCase,
    private val audioPlayer: AudioPlayer
) {

    private val _uiState = MutableStateFlow<UiState<SurahDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<SurahDetail>> = _uiState

    private val _playingAudioUrl = MutableStateFlow<String?>(null)
    val playingAudioUrl: StateFlow<String?> = _playingAudioUrl

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadSurah(surahNumber: Int) {
        scope.launch {
            _uiState.value = UiState.Loading
            getSurahDetailUseCase(surahNumber)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Gagal memuat detail") }
        }
    }

    fun playAudio(url: String) {
        if (_playingAudioUrl.value == url) {
            audioPlayer.pause()
            _playingAudioUrl.value = null
        } else {
            audioPlayer.play(url, onCompletion = {
                _playingAudioUrl.value = null
            })
            _playingAudioUrl.value = url
        }
    }

    fun onCleared() {
        audioPlayer.stop()
    }
}
