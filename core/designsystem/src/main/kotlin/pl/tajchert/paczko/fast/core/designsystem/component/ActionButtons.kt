package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Primary amber action button, e.g. "Open box remotely".
 * Full width, 50dp tall, Space Grotesk label.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = PaczkofastTheme.colors.accent,
            contentColor = PaczkofastTheme.colors.onAccent,
            disabledContainerColor = PaczkofastTheme.colors.accentDisabled,
            disabledContentColor = PaczkofastTheme.colors.onAccentDisabled,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = PaczkofastTheme.colors.onAccentDisabled,
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Secondary outlined action button, e.g. "Navigate" on the locker card.
 * Full width, 42dp tall, subtle amber-tinted border.
 */
@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, PaczkofastTheme.colors.outlineButtonBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PaczkofastTheme.colors.textPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

/**
 * Amber pill that fires [onConfirmed] only after a sustained press-and-hold of
 * [holdDurationMillis]. Releasing early animates the fill back to zero and does
 * nothing. A completed hold plays a confirmation haptic and fires exactly once.
 */
@Composable
fun HoldToConfirmButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDurationMillis: Int = 1200,
) {
    val controller = remember(holdDurationMillis) { HoldProgress(holdDurationMillis.toLong()) }
    val fill = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    val currentOnConfirmed by rememberUpdatedState(onConfirmed)
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed, enabled) {
        if (!pressed || !enabled) {
            controller.onRelease()
            if (fill.value != 0f) fill.animateTo(0f, tween(180))
            return@LaunchedEffect
        }
        var started = false
        while (pressed) {
            val frame = withFrameMillis { it }
            if (!started) {
                controller.onPress(frame)
                started = true
            }
            fill.snapTo(controller.progressAt(frame))
            if (controller.consumeCompletion(frame)) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                currentOnConfirmed()
            }
            if (fill.value >= 1f) break
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) PaczkofastTheme.colors.accent else PaczkofastTheme.colors.accentDisabled,
        contentColor = if (enabled) PaczkofastTheme.colors.onAccent else PaczkofastTheme.colors.onAccentDisabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { fill.value },
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { alpha = if (fill.value > 0f) 1f else 0.35f },
                    strokeWidth = 2.5.dp,
                    color = if (enabled) PaczkofastTheme.colors.onAccent else PaczkofastTheme.colors.onAccentDisabled,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun ActionButtonsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryActionButton(text = "Open box remotely", onClick = {})
            PrimaryActionButton(text = "Open box remotely", onClick = {}, isLoading = true)
            HoldToConfirmButton(text = "Hold to open", onConfirmed = {})
            HoldToConfirmButton(text = "Hold to open", onConfirmed = {}, enabled = false)
            OutlinedActionButton(text = "Navigate", onClick = {})
        }
    }
}
