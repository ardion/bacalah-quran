package id.ardion.quran.state

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>

    data class Error(val cause: Throwable) : UiState<Nothing>
}
