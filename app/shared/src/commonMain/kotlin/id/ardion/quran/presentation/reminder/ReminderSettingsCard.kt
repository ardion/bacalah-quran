package id.ardion.quran.presentation.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import bacalah.app.shared.generated.resources.Res
import bacalah.app.shared.generated.resources.reminder_adzan_credit
import bacalah.app.shared.generated.resources.reminder_enable_hint
import bacalah.app.shared.generated.resources.reminder_exact_alarm_action
import bacalah.app.shared.generated.resources.reminder_exact_alarm_dialog_body
import bacalah.app.shared.generated.resources.reminder_exact_alarm_dialog_confirm
import bacalah.app.shared.generated.resources.reminder_exact_alarm_dialog_dismiss
import bacalah.app.shared.generated.resources.reminder_exact_alarm_dialog_title
import bacalah.app.shared.generated.resources.reminder_exact_alarm_warning
import bacalah.app.shared.generated.resources.reminder_mode_adzan
import bacalah.app.shared.generated.resources.reminder_mode_notification
import bacalah.app.shared.generated.resources.reminder_mode_off
import bacalah.app.shared.generated.resources.reminder_permission_denied
import bacalah.app.shared.generated.resources.reminder_title
import id.ardion.quran.domain.model.Prayer
import id.ardion.quran.domain.model.ReminderMode
import id.ardion.quran.presentation.theme.IslamicPrimary
import id.ardion.quran.presentation.theme.IslamicTextPrimary
import id.ardion.quran.presentation.theme.IslamicTextSecondary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val PrayerNameWidth = 64.dp

@Composable
fun ReminderSettingsCard(viewModel: ReminderSettingsViewModel) {
    val state by viewModel.state.collectAsState()
    if (!state.isSupported) return

    val requester = rememberNotificationPermissionRequester()
    var isPermissionDenied by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect(viewModel) {
        viewModel.refreshExactAlarmStatus()
        onPauseOrDispose { }
    }

    if (showExactAlarmDialog) {
        ExactAlarmDialog(
            onConfirm = {
                showExactAlarmDialog = false
                viewModel.openExactAlarmSettings()
            },
            onDismiss = { showExactAlarmDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReminderHeader(
                isEnabled = state.settings.isEnabled,
                onToggle = { enable ->
                    if (!enable) {
                        viewModel.setEnabled(false)
                    } else {
                        requester.request { granted ->
                            isPermissionDenied = !granted
                            if (granted) {
                                viewModel.setEnabled(true)
                                showExactAlarmDialog = !viewModel.state.value.canScheduleExact
                            }
                        }
                    }
                }
            )
            if (isPermissionDenied) HintText(stringResource(Res.string.reminder_permission_denied))
            if (state.settings.isEnabled) {
                Prayer.entries.forEach { prayer ->
                    PrayerModeRow(
                        prayer = prayer,
                        selected = state.settings.modeFor(prayer),
                        onSelect = { viewModel.setMode(prayer, it) }
                    )
                }
                if (!state.canScheduleExact) ExactAlarmWarning(onAction = viewModel::openExactAlarmSettings)
                HintText(stringResource(Res.string.reminder_adzan_credit))
            }
        }
    }
}

@Composable
private fun ReminderHeader(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.reminder_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IslamicTextPrimary
            )
            HintText(stringResource(Res.string.reminder_enable_hint))
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = IslamicPrimary)
        )
    }
}

@Composable
private fun PrayerModeRow(prayer: Prayer, selected: ReminderMode, onSelect: (ReminderMode) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(prayer.nameResource()),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = IslamicTextPrimary,
            modifier = Modifier.width(PrayerNameWidth)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
            ReminderMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selected,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, ReminderMode.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IslamicPrimary.copy(alpha = 0.12f),
                        activeContentColor = IslamicPrimary
                    ),
                    label = { Text(stringResource(mode.labelResource())) }
                )
            }
        }
    }
}

@Composable
private fun ExactAlarmWarning(onAction: () -> Unit) {
    Column {
        HintText(stringResource(Res.string.reminder_exact_alarm_warning))
        TextButton(onClick = onAction) {
            Text(stringResource(Res.string.reminder_exact_alarm_action), color = IslamicPrimary)
        }
    }
}

@Composable
private fun ExactAlarmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(stringResource(Res.string.reminder_exact_alarm_dialog_title), color = IslamicTextPrimary) },
        text = { Text(stringResource(Res.string.reminder_exact_alarm_dialog_body), color = IslamicTextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.reminder_exact_alarm_dialog_confirm), color = IslamicPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.reminder_exact_alarm_dialog_dismiss), color = IslamicTextSecondary)
            }
        }
    )
}

@Composable
private fun HintText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = IslamicTextSecondary)
}

private fun ReminderMode.labelResource(): StringResource = when (this) {
    ReminderMode.OFF -> Res.string.reminder_mode_off
    ReminderMode.NOTIFICATION -> Res.string.reminder_mode_notification
    ReminderMode.ADZAN -> Res.string.reminder_mode_adzan
}
