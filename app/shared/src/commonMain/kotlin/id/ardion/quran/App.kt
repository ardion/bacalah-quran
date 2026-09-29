package id.ardion.quran

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import id.ardion.quran.di.appModule
import id.ardion.quran.presentation.surah.SurahDetailScreen
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListScreen
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        MaterialTheme {
            var selectedSurahNumber by remember { mutableStateOf<Int?>(null) }

            if (selectedSurahNumber == null) {
                val listViewModel: SurahListViewModel = koinInject()
                SurahListScreen(
                    viewModel = listViewModel,
                    onSurahClick = { surahNum -> selectedSurahNumber = surahNum }
                )
            } else {
                val detailViewModel: SurahDetailViewModel = koinInject()
                LaunchedEffect(selectedSurahNumber) {
                    selectedSurahNumber?.let { detailViewModel.loadSurah(it) }
                }
                SurahDetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { selectedSurahNumber = null }
                )
            }
        }
    }
}
