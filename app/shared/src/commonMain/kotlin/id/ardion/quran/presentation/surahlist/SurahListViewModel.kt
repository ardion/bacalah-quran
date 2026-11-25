package id.ardion.quran.presentation.surahlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SurahListViewModel(
    private val getSurahList: GetSurahListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SurahItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SurahItem>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allSurahs: List<SurahItem> = emptyList()
    private var loadJob: Job? = null

    fun loadSurahList() {
        if (allSurahs.isNotEmpty()) {
            showFiltered()
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = UiState.Loading
            getSurahList()
                .onSuccess {
                    allSurahs = it
                    showFiltered()
                }
                .onFailure { _uiState.value = UiState.Error(it) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (allSurahs.isNotEmpty()) showFiltered()
    }

    private fun showFiltered() {
        _uiState.value = UiState.Success(allSurahs.filterBy(_searchQuery.value))
    }
}

internal fun List<SurahItem>.filterBy(query: String): List<SurahItem> {
    if (query.isBlank()) return this
    return filter { surah ->
        surah.englishName.contains(query, ignoreCase = true) ||
            surah.number.toString().contains(query) ||
            surah.translation.contains(query, ignoreCase = true) ||
            surah.name.contains(query)
    }
}
