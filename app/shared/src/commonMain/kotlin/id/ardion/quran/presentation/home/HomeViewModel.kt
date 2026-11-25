package id.ardion.quran.presentation.home

import androidx.lifecycle.ViewModel
import id.ardion.quran.domain.model.LastRead
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(getLastRead: GetLastReadUseCase) : ViewModel() {
    val lastRead: StateFlow<LastRead> = getLastRead()
}
