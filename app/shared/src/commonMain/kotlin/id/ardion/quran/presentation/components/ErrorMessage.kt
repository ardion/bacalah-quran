package id.ardion.quran.presentation.components

import androidx.compose.runtime.Composable
import bacalah.app.shared.generated.resources.*
import id.ardion.quran.exception.NetworkException
import org.jetbrains.compose.resources.stringResource

@Composable
fun errorMessage(cause: Throwable): String = when (cause) {
    NetworkException.NoInternet -> stringResource(Res.string.error_no_internet)
    NetworkException.Timeout -> stringResource(Res.string.error_timeout)
    NetworkException.Unauthorized, NetworkException.Forbidden -> stringResource(Res.string.error_forbidden)
    NetworkException.NotFound, NetworkException.EmptyResponse -> stringResource(Res.string.error_not_found)
    is NetworkException.ServerError -> stringResource(Res.string.error_server, cause.statusCode)
    is NetworkException.ClientError -> stringResource(Res.string.error_client, cause.statusCode)
    else -> stringResource(Res.string.error_general)
}
