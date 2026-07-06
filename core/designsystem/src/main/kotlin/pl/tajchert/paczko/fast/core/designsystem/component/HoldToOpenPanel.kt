package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.designsystem.theme.SpaceGroteskFamily

/**
 * The "armed" open-box panel (design 6d): a large ring wrapping a live distance
 * readout, a hold prompt, and a full-width hold-to-open bar at the bottom. The
 * ring fills in step with the hold — [onConfirmed] fires exactly once, only
 * after a completed hold; releasing early animates everything back to zero.
 *
 * @param distanceText Center readout, e.g. "8 m" (shows "—" when null).
 * @param lockerCaption Small caption under the distance, e.g. "to locker WAW01A".
 * @param subline Secondary line under the prompt, e.g. "Parcel · box pops open".
 */
@Composable
fun HoldToOpenPanel(
    distanceText: String?,
    lockerCaption: String,
    subline: String?,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    holdDurationMillis: Int = 1200,
) {
    val hold = rememberHoldToOpenState(
        holdDurationMillis = holdDurationMillis,
        enabled = enabled,
        onConfirmed = onConfirmed,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                stateDescription = if (enabled) {
                    "Ready to open"
                } else {
                    "Disabled"
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DistanceRing(
                progress = hold.progress,
                distanceText = distanceText ?: "—",
                caption = lockerCaption,
            )
            Column(
                modifier = Modifier.padding(top = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (hold.isHolding) "Keep holding…" else "Hold to open",
                    style = MaterialTheme.typography.displaySmall,
                    color = PaczkofastTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                subline?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PaczkofastTheme.colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        HoldBar(state = hold)
        Text(
            text = "Release to cancel — nothing opens until the ring completes",
            style = MaterialTheme.typography.bodySmall,
            color = PaczkofastTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

/**
 * State + press handling for a hold-to-open interaction (design 5a/6d): tracks
 * hold [progress] via [HoldProgress] and an [Animatable] fill, fires
 * [onConfirmed] exactly once when a completed hold is detected (with a haptic
 * tick), and animates back to zero when released early. Apply [pressModifier]
 * to whatever surface should be pressed and held (e.g. [HoldBar]).
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

@Composable
fun DistanceRing(
    progress: Float,
    distanceText: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val trackColor = PaczkofastTheme.colors.trackBackground
    val accent = PaczkofastTheme.colors.accent
    Box(
        modifier = Modifier
            .size(216.dp)
            .then(modifier)
            .semantics {
                contentDescription = "$distanceText, $caption"
                progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(216.dp)) {
            val stroke = 6.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = distanceText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                ),
                color = PaczkofastTheme.colors.textPrimary,
            )
            Text(
                text = caption.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = PaczkofastTheme.colors.textMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun HoldBar(
    state: HoldToOpenState,
    label: String = "Hold to open",
    modifier: Modifier = Modifier,
) {
    val holding = state.isHolding
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (holding) {
            HoldProgressBar(progress = state.progress)
        }
        NeoSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .then(state.pressModifier)
                .semantics {
                    contentDescription = label
                    stateDescription = when {
                        !state.isEnabled -> "Disabled"
                        holding -> "Keep holding"
                        else -> "Press and hold to open"
                    }
                },
            shape = RoundedCornerShape(14.dp),
            fill = PaczkofastTheme.colors.accent,
            borderColor = PaczkofastTheme.colors.borderStrong,
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
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = PaczkofastTheme.colors.onAccent,
                )
            }
        }
    }
}

/**
 * "KEEP HOLDING…" mono caption above a thick bordered progress bar (design 5a):
 * the ink-outlined track fills with accent yellow up to [progress], with a
 * small ink divider marking the fill's leading edge.
 */
@Composable
private fun HoldProgressBar(progress: Float) {
    val clamped = progress.coerceIn(0f, 1f)
    Column(
        modifier = Modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f)
            stateDescription = "${(clamped * 100).toInt()} percent"
        },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Keep holding…".uppercase(),
            style = MonoLabel,
            color = PaczkofastTheme.colors.monoLabel,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PaczkofastTheme.colors.trackBackground)
                .border(2.5.dp, PaczkofastTheme.colors.borderStrong, RoundedCornerShape(8.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .align(Alignment.CenterStart),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PaczkofastTheme.colors.accent),
                )
                if (clamped > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(2.5.dp)
                            .background(PaczkofastTheme.colors.borderStrong),
                    )
                }
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun HoldToOpenPanelPreview() {
    PaczkofastTheme {
        HoldToOpenPanel(
            distanceText = "8 m",
            lockerCaption = "to locker WAW01A",
            subline = "Box pops open below eye level",
            onConfirmed = {},
            modifier = Modifier.padding(20.dp),
        )
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
