package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import kotlin.math.min

/**
 * Neo-brutalist pull-to-refresh indicator: the app's yellow logo tile (ink
 * border, hard shadow, rotated ink diamond) that drops in with the pull and
 * spins its diamond while refreshing. Replaces the default Material spinner so
 * the gesture matches the rest of the app.
 *
 * Place inside [androidx.compose.material3.pulltorefresh.PullToRefreshBox]'s
 * `indicator` slot, aligned to the top center.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaczkofastPullRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
) {
    // How far the user has pulled; 1f = at the trigger threshold.
    val pull = state.distanceFraction
    val settle = if (isRefreshing) 1f else min(pull, 1f)

    val spin = rememberInfiniteTransition(label = "refreshSpin")
    val spinAngle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "refreshSpinAngle",
    )

    // Before release the diamond tips over with the pull; refreshing → spins.
    val diamondAngle = if (isRefreshing) spinAngle else 45f + pull * 160f

    Box(
        modifier = modifier
            .padding(top = 10.dp)
            .graphicsLayer {
                // Rise in from above the top edge as the pull grows.
                translationY = (settle - 1f) * 24.dp.toPx()
                alpha = if (isRefreshing) 1f else min(pull * 1.4f, 1f)
                val scale = 0.8f + 0.2f * settle
                scaleX = scale
                scaleY = scale
            },
    ) {
        NeoSurface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(13.dp),
            fill = PaczkofastTheme.colors.accent,
            borderColor = PaczkofastTheme.colors.accentBorder,
            shadowOffset = 3.dp,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(14.dp)
                    .rotate(diamondAngle)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PaczkofastTheme.colors.onAccent),
            )
        }
    }
}
