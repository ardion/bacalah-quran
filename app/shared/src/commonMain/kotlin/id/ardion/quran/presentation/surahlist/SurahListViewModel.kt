package id.ardion.quran.presentation.surahlist

import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SurahListViewModel(
    private val getSurahListUseCase: GetSurahListUseCase
) {

    private val _uiState = MutableStateFlow<UiState<List<SurahItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SurahItem>>> = _uiState

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadSurahList()
    }

    fun loadSurahList() {
        scope.launch {
            _uiState.value = UiState.Loading
            getSurahListUseCase()
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Gagal memuat surah") }
        }
    }
}
