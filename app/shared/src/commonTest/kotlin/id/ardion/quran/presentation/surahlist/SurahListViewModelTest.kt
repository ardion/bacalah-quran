package id.ardion.quran.presentation.surahlist

import id.ardion.quran.domain.model.SurahItem
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.fake.FakeQuranRepository
import id.ardion.quran.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SurahListViewModelTest {
    private val repository = FakeQuranRepository()
    private val surahs = listOf(
        SurahItem(1, "الفاتحة", "Al-Faatiha", "The Opening", 7, "Meccan"),
        SurahItem(36, "يس", "Yaseen", "Yaseen", 83, "Meccan")
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadShowsAllSurahs() {
        repository.surahs = Result.success(surahs)
        val viewModel = SurahListViewModel(GetSurahListUseCase(repository))
        viewModel.loadSurahList()
        assertEquals(surahs, assertIs<UiState.Success<List<SurahItem>>>(viewModel.uiState.value).data)
    }

    @Test
    fun searchFiltersLoadedList() {
        repository.surahs = Result.success(surahs)
        val viewModel = SurahListViewModel(GetSurahListUseCase(repository))
        viewModel.loadSurahList()
        viewModel.onSearchQueryChanged("yas")
        assertEquals(listOf(36), assertIs<UiState.Success<List<SurahItem>>>(viewModel.uiState.value).data.map { it.number })
    }

    @Test
    fun failureShowsErrorThenRetrySucceeds() {
        repository.surahs = Result.failure(IllegalStateException("offline"))
        val viewModel = SurahListViewModel(GetSurahListUseCase(repository))
        viewModel.loadSurahList()
        assertIs<UiState.Error>(viewModel.uiState.value)

        repository.surahs = Result.success(surahs)
        viewModel.loadSurahList()
        assertIs<UiState.Success<List<SurahItem>>>(viewModel.uiState.value)
    }
}
