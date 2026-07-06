package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val ActionButtonShape = RoundedCornerShape(12.dp)

/**
 * Primary yellow action button, e.g. "Open locker" / "Open box remotely".
 * A [NeoSurface] with an ink border and hard offset shadow, ~46dp tall,
 * Space Grotesk label.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    accessibilityLabel: String = text,
) {
    val colors = PaczkofastTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isEnabled = enabled && !isLoading

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                role = Role.Button,
                onClickLabel = accessibilityLabel,
                onClick = onClick,
            )
            .then(Modifier.actionLoadingSemantics(isLoading, accessibilityLabel)),
        shape = ActionButtonShape,
        fill = if (isEnabled) colors.accent else colors.accentDisabled,
        borderColor = colors.accentBorder,
        shadow = isEnabled,
        pressed = pressed && isEnabled,
    ) {
        Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = colors.onAccentDisabled,
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isEnabled) colors.onAccent else colors.onAccentDisabled,
                )
            }
        }
    }
}

/**
 * Secondary outlined action button, e.g. "Navigate" on the locker card. A
 * white [NeoSurface] with an ink border and a lighter hard shadow, ~42dp
 * tall.
 */
@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String = text,
) {
    val colors = PaczkofastTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = accessibilityLabel,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
        shape = ActionButtonShape,
        fill = colors.cardSurface,
        borderColor = colors.borderStrong,
        shadowOffset = 2.dp,
        pressed = pressed && enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) colors.textPrimary else colors.textFaint,
            modifier = Modifier.align(Alignment.Center),
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
    accessibilityLabel: String = text,
) {
    val colors = PaczkofastTheme.colors
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) colors.accent else colors.accentDisabled)
            .border(2.5.dp, colors.borderStrong, RoundedCornerShape(14.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                )
            }
            .semantics {
                contentDescription = accessibilityLabel
                stateDescription = if (enabled) {
                    "Press and hold to confirm"
                } else {
                    "Disabled"
                }
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
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
                    color = if (enabled) colors.onAccent else colors.onAccentDisabled,
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) colors.onAccent else colors.onAccentDisabled,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

private fun Modifier.actionLoadingSemantics(
    isLoading: Boolean,
    accessibilityLabel: String,
): Modifier = if (isLoading) {
    semantics {
        role = Role.Button
        contentDescription = accessibilityLabel
        stateDescription = "Loading"
    }
} else {
    semantics { role = Role.Button }
}

@PaczkofastPreviews
@Composable
private fun ActionButtonsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryActionButton(text = "Open locker", onClick = {})
            PrimaryActionButton(text = "Open locker", onClick = {}, isLoading = true)
            HoldToConfirmButton(text = "Hold to open", onConfirmed = {})
            HoldToConfirmButton(text = "Hold to open", onConfirmed = {}, enabled = false)
            OutlinedActionButton(text = "Navigate", onClick = {})
        }
    }
}
