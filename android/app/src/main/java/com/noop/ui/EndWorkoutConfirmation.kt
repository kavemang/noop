package com.noop.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.noop.R

/**
 * Shared destructive confirmation for every manual-workout stop affordance (#517).
 *
 * Keeping the dialog in one place prevents the compact Live/Workouts cards and the full-screen
 * workout view from drifting back to one-tap termination independently.
 */
@Composable
internal fun EndWorkoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceOverlay,
        title = {
            Text(
                uiString(R.string.l10n_end_workout_confirmation_end_this_workout_91ecdb4c),
                style = NoopType.title2,
                color = Palette.textPrimary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    uiString(R.string.l10n_live_workout_screen_end_workout_3e8d6238),
                    style = NoopType.body,
                    color = Palette.statusCritical,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    uiString(R.string.l10n_workout_start_cancel_77dfd213),
                    style = NoopType.body,
                    color = Palette.textSecondary,
                )
            }
        },
    )
}
