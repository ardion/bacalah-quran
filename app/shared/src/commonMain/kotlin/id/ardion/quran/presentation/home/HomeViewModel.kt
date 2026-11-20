package id.ardion.quran.presentation.home

import androidx.lifecycle.ViewModel
import id.ardion.quran.domain.model.MultiLastReadInfo
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val getLastReadUseCase: GetLastReadUseCase
) : ViewModel() {

    val multiLastRead: StateFlow<MultiLastReadInfo> = getLastReadUseCase()
}
