package id.ardion.quran.presentation.mushaf

import id.ardion.quran.audio.AudioPlayer
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MushafPageViewModel(
    private val getPageDetailUseCase: GetPageDetailUseCase,
    private val audioPlayer: AudioPlayer
) {

    private val pageCache = mutableMapOf<Int, UiState<PageDetail>>()

    private val _uiState = MutableStateFlow<UiState<PageDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<PageDetail>> = _uiState

    private val _playingAudioUrl = MutableStateFlow<String?>(null)
    val playingAudioUrl: StateFlow<String?> = _playingAudioUrl

    private val _isPlayingPage = MutableStateFlow(false)
    val isPlayingPage: StateFlow<Boolean> = _isPlayingPage

    private var currentPlayingAyahIndex: Int = 0
    private var currentPage: Int = 0

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadPage(pageNumber: Int) {
        if (currentPage == pageNumber && _uiState.value is UiState.Success) return
        stopAudio()
        currentPage = pageNumber
        currentPlayingAyahIndex = 0

        pageCache[pageNumber]?.let { cachedState ->
            _uiState.value = cachedState
            return
        }

        _uiState.value = UiState.Loading
        scope.launch {
            getPageDetailUseCase(pageNumber)
                .onSuccess { pageDetail ->
                    val successState = UiState.Success(pageDetail)
                    pageCache[pageNumber] = successState
                    if (currentPage == pageNumber) {
                        _uiState.value = successState
                    }
                }
                .onFailure { error ->
                    val errorState = UiState.Error(error.message ?: "Gagal memuat halaman $pageNumber")
                    if (currentPage == pageNumber) {
                        _uiState.value = errorState
                    }
                }
        }
    }

    fun playAudio(url: String) {
        _isPlayingPage.value = false
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

    fun togglePlayPage(ayahs: List<AyahDetail>) {
        if (_isPlayingPage.value || _playingAudioUrl.value != null) {
            pausePageAudio()
        } else {
            _isPlayingPage.value = true
            playAyahAtIndex(currentPlayingAyahIndex, ayahs)
        }
    }

    private fun playAyahAtIndex(index: Int, ayahs: List<AyahDetail>) {
        if (index >= ayahs.size || !_isPlayingPage.value) {
            stopAudio()
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
            if (_isPlayingPage.value) {
                playAyahAtIndex(index + 1, ayahs)
            } else {
                _playingAudioUrl.value = null
                _isPlayingPage.value = false
            }
        })
    }

    fun pausePageAudio() {
        _isPlayingPage.value = false
        _playingAudioUrl.value = null
        audioPlayer.pause()
    }

    fun stopAudio() {
        _isPlayingPage.value = false
        _playingAudioUrl.value = null
        currentPlayingAyahIndex = 0
        audioPlayer.stop()
    }

    fun onCleared() {
        stopAudio()
    }
}
