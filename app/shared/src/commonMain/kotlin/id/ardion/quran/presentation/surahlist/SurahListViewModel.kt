package id.ardion.quran.presentation.surahlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.state.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SurahListViewModel(
    private val getSurahListUseCase: GetSurahListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<SurahItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<SurahItem>>> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var fullSurahList: List<SurahItem> = emptyList()

    fun loadSurahList() {
        if (fullSurahList.isNotEmpty()) {
            _uiState.value = UiState.Success(fullSurahList)
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val list = getSurahListUseCase()
                fullSurahList = list
                filterSurahList(_searchQuery.value)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _uiState.value = UiState.Error(e.message ?: "Gagal memuat surah")
            }
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
