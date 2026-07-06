package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * State + press handling for a hold-to-open interaction (design 5a): tracks
 * hold [progress] via [HoldProgress] and an [Animatable] fill, fires
 * [onConfirmed] exactly once when a completed hold is detected (with a haptic
 * tick), and animates back to zero when released early. Apply [pressModifier]
 * to whatever surface should be pressed and held (e.g. [HoldBar]); the same
 * [progress] drives the [HoldRing] hero so the ring fills as the user holds.
 */
@Stable
class HoldToOpenState internal constructor(
    val progress: Float,
    val isHolding: Boolean,
    val isEnabled: Boolean,
    val pressModifier: Modifier,
)

@Composable
fun rememberHoldToOpenState(
    holdDurationMillis: Int = 1200,
    enabled: Boolean = true,
    onConfirmed: () -> Unit,
): HoldToOpenState {
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

    val pressModifier = Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(onPress = {
            pressed = true
            tryAwaitRelease()
            pressed = false
        })
    }
    return HoldToOpenState(
        progress = fill.value,
        isHolding = pressed && enabled,
        isEnabled = enabled,
        pressModifier = pressModifier,
    )
}

/** Diameter of the collect hero. Also the [HoldRing] outer ring. */
val HeroSize = 216.dp

/**
 * Diameter of the central yellow blob shared by the [HoldRing] (its lock core)
 * and the terminal collect heroes (box / check / error), so the icon appears to
 * stay put while the ring recedes on success ("sinks under the icon").
 */
val HeroBlobSize = 108.dp

private val MiddleRingSize = 160.dp

/**
 * Concentric hold-to-open ring (design 5a): a thick outer band that fills yellow
 * clockwise from the top as [progress] grows (the rest is [ringTrack]), a middle
 * ring in the screen color, and a central yellow blob holding [glyph] (a padlock
 * by default). Yellow surfaces keep an ink border in both themes; the hard shadow
 * follows the theme foreground.
 */
@Composable
fun HoldRing(
    progress: Float,
    modifier: Modifier = Modifier,
    glyph: @Composable () -> Unit = { HoldRingLockGlyph() },
) {
    val colors = PaczkofastTheme.colors
    val clamped = progress.coerceIn(0f, 1f)
    val holdToOpen = stringResource(R.string.hold_to_open)
    Box(
        modifier = Modifier
            .size(HeroSize)
            .then(modifier)
            .semantics {
                contentDescription = holdToOpen
                progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        // Outer conic disc: a yellow pie up to `progress`, the remainder in the
        // ring track. The middle ring below covers the center, leaving a band.
        Box(
            modifier = Modifier
                .size(HeroSize)
                .hardShadow(4.dp, 4.dp, colors.hardShadow, CircleShape)
                .clip(CircleShape)
                .border(3.dp, colors.accentBorder, CircleShape),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(color = colors.ringTrack, startAngle = -90f, sweepAngle = 360f, useCenter = true)
                if (clamped > 0f) {
                    drawArc(
                        color = colors.accent,
                        startAngle = -90f,
                        sweepAngle = 360f * clamped,
                        useCenter = true,
                    )
                }
            }
        }
        // Middle ring in the screen color hides the pie center, leaving the band.
        Box(
            modifier = Modifier
                .size(MiddleRingSize)
                .clip(CircleShape)
                .background(colors.background)
                .border(3.dp, colors.borderStrong, CircleShape),
        )
        // Central yellow blob with the glyph.
        Box(
            modifier = Modifier
                .size(HeroBlobSize)
                .hardShadow(4.dp, 4.dp, colors.hardShadow, CircleShape)
                .clip(CircleShape)
                .background(colors.accent)
                .border(3.dp, colors.accentBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            glyph()
        }
    }
}

@Composable
private fun HoldRingLockGlyph() {
    Icon(
        imageVector = Icons.Rounded.Lock,
        contentDescription = null,
        tint = PaczkofastTheme.colors.onAccent,
        modifier = Modifier.size(44.dp),
    )
}

/**
 * The yellow "Hold to open" action bar (design 5a). Press-and-hold via
 * [HoldToOpenState.pressModifier]; the hold fills the [HoldRing] hero rather
 * than a separate progress bar. Shows a spinner while holding.
 */
@Composable
fun HoldBar(
    state: HoldToOpenState,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    val holding = state.isHolding
    val resolvedLabel = label ?: stringResource(R.string.hold_to_open)
    val disabled = stringResource(R.string.disabled)
    val keepHolding = stringResource(R.string.keep_holding)
    val pressAndHold = stringResource(R.string.press_and_hold_to_open)
    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(state.pressModifier)
            .semantics {
                contentDescription = resolvedLabel
                stateDescription = when {
                    !state.isEnabled -> disabled
                    holding -> keepHolding
                    else -> pressAndHold
                }
            },
        shape = RoundedCornerShape(14.dp),
        fill = PaczkofastTheme.colors.accent,
        borderColor = PaczkofastTheme.colors.accentBorder,
        pressed = holding,
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (holding) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.5.dp,
                    color = PaczkofastTheme.colors.onAccent,
                )
            }
            Text(
                text = resolvedLabel,
                style = MaterialTheme.typography.labelLarge,
                color = PaczkofastTheme.colors.onAccent,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun HoldRingHalfPreview() {
    PaczkofastTheme {
        HoldRing(progress = 0.62f, modifier = Modifier.padding(20.dp))
    }
}

@PaczkofastPreviews
@Composable
private fun HoldBarHoldingPreview() {
    PaczkofastTheme {
        HoldBar(
            state = HoldToOpenState(
                progress = 0.6f,
                isHolding = true,
                isEnabled = true,
                pressModifier = Modifier,
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun HoldBarIdlePreview() {
    PaczkofastTheme {
        HoldBar(
            state = HoldToOpenState(
                progress = 0f,
                isHolding = false,
                isEnabled = true,
                pressModifier = Modifier,
            ),
            modifier = Modifier.padding(20.dp),
        )
    }
}
