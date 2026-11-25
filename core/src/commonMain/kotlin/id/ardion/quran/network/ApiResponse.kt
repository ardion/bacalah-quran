package id.ardion.quran.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("code") val code: Int? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("data") val data: T? = null
)
