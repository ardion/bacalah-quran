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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var fullSurahList: List<SurahItem> = emptyList()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        loadSurahList()
    }

    fun loadSurahList() {
        scope.launch {
            _uiState.value = UiState.Loading
            getSurahListUseCase()
                .onSuccess { list ->
                    fullSurahList = list
                    filterSurahList(_searchQuery.value)
                }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "Gagal memuat surah") }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterSurahList(query)
    }

    private fun filterSurahList(query: String) {
        if (query.isBlank()) {
            _uiState.value = UiState.Success(fullSurahList)
        } else {
            val filtered = fullSurahList.filter { surah ->
                surah.englishName.contains(query, ignoreCase = true) ||
                surah.number.toString().contains(query) ||
                surah.translation.contains(query, ignoreCase = true) ||
                surah.name.contains(query)
            }
            _uiState.value = UiState.Success(filtered)
        }
    }
}
