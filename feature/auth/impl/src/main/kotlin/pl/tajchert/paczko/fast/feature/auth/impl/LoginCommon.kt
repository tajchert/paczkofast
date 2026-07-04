package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/** "500100200" → "500 100 200"; partial input keeps completed groups. */
internal fun formatPhoneDigits(digits: String): String =
    digits.chunked(3).joinToString(" ")

/**
 * Blinking caret used by the phone field (amber, on a white surface) and the
 * active OTP code box (ink, on a yellow surface) — [color] picks the tint.
 */
@Composable
internal fun BlinkingCursor(
    height: Dp,
    modifier: Modifier = Modifier,
    color: Color = PaczkofastTheme.colors.accent,
) {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1_000
                1f at 0
                1f at 480
                0f at 500
                0f at 980 using LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "cursorAlpha",
    )
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(width = 2.dp, height = height)
            .alpha(alpha)
            .background(color),
    )
}

/**
 * Tall (56dp) primary button of the login flow — "Send code" / "Log in".
 * Dimmed amber when disabled, per the design's incomplete-code state.
 */
@Composable
internal fun LoginPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PaczkofastTheme.colors.accent,
            contentColor = PaczkofastTheme.colors.onAccent,
            disabledContainerColor = PaczkofastTheme.colors.accentDisabled,
            disabledContentColor = PaczkofastTheme.colors.onAccentDisabled,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
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
