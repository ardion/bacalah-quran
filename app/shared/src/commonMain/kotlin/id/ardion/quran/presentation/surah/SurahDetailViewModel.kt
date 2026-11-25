package id.ardion.quran.presentation.surah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.audio.AudioQueueController
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.model.SurahLastRead
import id.ardion.quran.domain.usecase.ClearSurahLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.SaveSurahLastReadUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SurahDetailViewModel(
    private val getSurahDetail: GetSurahDetailUseCase,
    getLastRead: GetLastReadUseCase,
    private val saveSurahLastRead: SaveSurahLastReadUseCase,
    private val clearSurahLastRead: ClearSurahLastReadUseCase,
    private val audio: AudioQueueController
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SurahDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<SurahDetail>> = _uiState.asStateFlow()

    val lastRead: StateFlow<SurahLastRead?> = getLastRead().let { flow ->
        flow.map { it.surah }.stateIn(viewModelScope, SharingStarted.Eagerly, flow.value.surah)
    }

    val playingAudioUrl: StateFlow<String?> = audio.playingUrl
    val isPlayingFullSurah: StateFlow<Boolean> = audio.isPlayingQueue
    val isAudioAvailable: Boolean = audio.isAvailable

    private var surahNumber: Int? = null
    private var loadJob: Job? = null

    fun loadSurah(surahNumber: Int) {
        this.surahNumber = surahNumber
        audio.stop()
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            getSurahDetail(surahNumber)
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it) }
        }
    }

    fun retry() {
        surahNumber?.let(::loadSurah)
    }

    fun toggleLastRead(surah: SurahDetail, ayah: AyahDetail) {
        val current = lastRead.value
        if (current?.surahNumber == surah.number && current.ayahNumber == ayah.numberInSurah) {
            clearSurahLastRead()
        } else {
            saveSurahLastRead(SurahLastRead(surah.number, surah.englishName, ayah.numberInSurah))
        }
    }

    fun playAudio(url: String) = audio.toggleSingle(url)

    fun togglePlayFullSurah(ayahs: List<AyahDetail>) = audio.toggleQueue(ayahs.map { it.audioUrl })

    fun stopAudio() = audio.stop()

    override fun onCleared() {
        super.onCleared()
        audio.stop()
    }
}
