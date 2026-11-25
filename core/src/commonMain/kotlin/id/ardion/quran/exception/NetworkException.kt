package id.ardion.quran.exception

sealed class NetworkException(message: String) : Exception(message) {
    object NoInternet : NetworkException("Koneksi internet terputus.")
    object Timeout : NetworkException("Waktu koneksi habis. Silakan coba lagi.")
    object Unauthorized : NetworkException("Akses tidak diizinkan. Silakan login kembali.")
    object Forbidden : NetworkException("Akses ditolak.")
    object NotFound : NetworkException("Data tidak ditemukan.")
    data class ClientError(val statusCode: Int) : NetworkException("Kesalahan permintaan ($statusCode).")
    data class ServerError(val statusCode: Int) : NetworkException("Terjadi kesalahan pada server ($statusCode).")
    object EmptyResponse : NetworkException("Data tidak tersedia.")
}
