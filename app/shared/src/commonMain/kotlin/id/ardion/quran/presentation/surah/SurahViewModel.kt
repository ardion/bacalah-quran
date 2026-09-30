package id.ardion.quran.presentation.surah

import id.ardion.quran.audio.AudioPlayer
import id.ardion.quran.domain.model.AyahDetail
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

    private val _isPlayingFullSurah = MutableStateFlow(false)
    val isPlayingFullSurah: StateFlow<Boolean> = _isPlayingFullSurah

    private var currentPlayingAyahIndex: Int = 0

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadSurah(surahNumber: Int) {
        currentPlayingAyahIndex = 0
        scope.launch {
            _uiState.value = UiState.Loading
            getSurahDetailUseCase(surahNumber)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Gagal memuat detail") }
        }
    }

    fun playAudio(url: String) {
        _isPlayingFullSurah.value = false
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

    fun togglePlayFullSurah(ayahs: List<AyahDetail>) {
        if (_isPlayingFullSurah.value) {
            pauseFullSurah()
        } else {
            _isPlayingFullSurah.value = true
            playAyahAtIndex(currentPlayingAyahIndex, ayahs)
        }
    }

    private fun playAyahAtIndex(index: Int, ayahs: List<AyahDetail>) {
        if (index >= ayahs.size || !_isPlayingFullSurah.value) {
            stopFullSurah()
            return
        }

        currentPlayingAyahIndex = index
        val currentAyah = ayahs[index]
        val audioUrl = currentAyah.audioUrl ?: run {
            playAyahAtIndex(index + 1, ayahs)
            return
        }

        _playingAudioUrl.value = audioUrl

        audioPlayer.play(audioUrl, onCompletion = {
            if (_isPlayingFullSurah.value) {
                playAyahAtIndex(index + 1, ayahs)
            } else {
                _playingAudioUrl.value = null
            }
        })
    }

    fun pauseFullSurah() {
        _isPlayingFullSurah.value = false
        audioPlayer.pause()
    }

    fun stopFullSurah() {
        _isPlayingFullSurah.value = false
        _playingAudioUrl.value = null
        currentPlayingAyahIndex = 0
        audioPlayer.stop()
    }

    fun onCleared() {
        stopFullSurah()
    }
}
