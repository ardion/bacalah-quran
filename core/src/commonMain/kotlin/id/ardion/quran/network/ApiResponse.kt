package id.ardion.quran.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("code") val code: Int,
    @SerialName("status") val status: String,
    @SerialName("data") val data: T
)
