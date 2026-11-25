package id.ardion.quran.presentation.surah

import id.ardion.quran.audio.AudioQueueController
import id.ardion.quran.domain.model.AyahDetail
import id.ardion.quran.domain.model.SurahDetail
import id.ardion.quran.domain.usecase.ClearSurahLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.SaveSurahLastReadUseCase
import id.ardion.quran.fake.FakeAudioPlayback
import id.ardion.quran.fake.FakeLastReadRepository
import id.ardion.quran.fake.FakeQuranRepository
import id.ardion.quran.state.UiState
import id.ardion.quran.tajweed.TajweedText
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SurahDetailViewModelTest {
    private val quran = FakeQuranRepository()
    private val lastRead = FakeLastReadRepository()
    private val ayah = AyahDetail(1, TajweedText("ب"), "", null, "https://audio/1.mp3")
    private val surah = SurahDetail(36, "يس", "Yaseen", "Meccan", 1, listOf(ayah))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SurahDetailViewModel(
        GetSurahDetailUseCase(quran),
        GetLastReadUseCase(lastRead),
        SaveSurahLastReadUseCase(lastRead),
        ClearSurahLastReadUseCase(lastRead),
        AudioQueueController(FakeAudioPlayback())
    )

    @Test
    fun toggleLastReadSavesThenClears() {
        val viewModel = viewModel()
        viewModel.toggleLastRead(surah, ayah)
        assertEquals(36, viewModel.lastRead.value?.surahNumber)

        viewModel.toggleLastRead(surah, ayah)
        assertNull(viewModel.lastRead.value)
    }

    @Test
    fun retryReloadsTheRequestedSurah() {
        quran.surahDetail = { Result.failure(IllegalStateException("offline")) }
        val viewModel = viewModel()
        viewModel.loadSurah(36)
        assertIs<UiState.Error>(viewModel.uiState.value)

        quran.surahDetail = { Result.success(surah) }
        viewModel.retry()
        assertEquals(listOf(36, 36), quran.surahRequests)
        assertIs<UiState.Success<SurahDetail>>(viewModel.uiState.value)
    }
}
