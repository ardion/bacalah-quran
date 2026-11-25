package id.ardion.quran.presentation.mushaf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.audio.AudioQueueController
import id.ardion.quran.cache.LruCache
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.MushafLastRead
import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.usecase.ClearMushafLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.SaveMushafLastReadUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val PAGE_CACHE_SIZE = 10

class MushafPageViewModel(
    private val getPageDetail: GetPageDetailUseCase,
    getLastRead: GetLastReadUseCase,
    private val saveMushafLastRead: SaveMushafLastReadUseCase,
    private val clearMushafLastRead: ClearMushafLastReadUseCase,
    private val audio: AudioQueueController
) : ViewModel() {

    private val pageCache = LruCache<Int, UiState.Success<PageDetail>>(PAGE_CACHE_SIZE)

    private val _uiState = MutableStateFlow<UiState<PageDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<PageDetail>> = _uiState.asStateFlow()

    val lastRead: StateFlow<MushafLastRead?> = getLastRead().let { flow ->
        flow.map { it.mushaf }.stateIn(viewModelScope, SharingStarted.Eagerly, flow.value.mushaf)
    }

    val initialPageNumber: Int = lastRead.value?.pageNumber ?: 1

    val playingAudioUrl: StateFlow<String?> = audio.playingUrl
    val isPlayingAudio: StateFlow<Boolean> = audio.isPlaying
    val isPlayingPage: StateFlow<Boolean> = audio.isPlayingQueue
    val isAudioAvailable: Boolean = audio.isAvailable

    private var currentPage: Int? = null
    private var loadJob: Job? = null

    fun loadPage(pageNumber: Int) {
        if (currentPage == pageNumber && _uiState.value is UiState.Success) return
        audio.stop()
        currentPage = pageNumber
        loadJob?.cancel()

        pageCache[pageNumber]?.let { cached ->
            _uiState.value = cached
            return
        }

        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            getPageDetail(pageNumber)
                .onSuccess { page ->
                    val success = UiState.Success(page)
                    pageCache[pageNumber] = success
                    _uiState.value = success
                }
                .onFailure { _uiState.value = UiState.Error(it) }
        }
    }

    fun toggleLastRead(pageNumber: Int, ayahNumber: Int, surahName: String?) {
        val current = lastRead.value
        if (current?.pageNumber == pageNumber && current.ayahNumber == ayahNumber && current.surahName == surahName) {
            clearMushafLastRead()
        } else {
            saveMushafLastRead(MushafLastRead(pageNumber, surahName, ayahNumber))
        }
    }

    fun playAudio(url: String) = audio.toggleSingle(url)

    fun togglePlayPage(ayahs: List<AyahDetail>) = audio.toggleQueue(ayahs.map { it.audioUrl })

    fun stopAudio() = audio.stop()

    override fun onCleared() {
        super.onCleared()
        audio.stop()
    }
}
