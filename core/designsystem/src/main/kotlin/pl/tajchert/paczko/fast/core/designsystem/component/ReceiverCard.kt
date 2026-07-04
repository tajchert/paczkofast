package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val ReceiverCardShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)

/**
 * Parcel receiver row on the parcel detail screen: a person icon, the
 * receiver's name (with a " · you" suffix when it's the current user) and
 * phone, and a small "RECEIVER" mono label.
 */
@Composable
fun ReceiverCard(
    name: String,
    modifier: Modifier = Modifier,
    phone: String? = null,
    isYou: Boolean = false,
) {
    val colors = PaczkofastTheme.colors
    NeoSurface(
        modifier = modifier.fillMaxWidth(),
        shape = ReceiverCardShape,
        fill = colors.cardSurface,
        borderColor = colors.borderStrong,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PersonIcon()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isYou) "$name · you" else name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.textPrimary,
                )
                phone?.let {
                    Text(
                        text = it,
                        style = MonoLabel,
                        color = colors.textMuted,
                    )
                }
            }
            Text(
                text = "RECEIVER",
                style = MonoLabel,
                color = colors.monoLabel,
            )
        }
    }
}

/** Simple line-art head-and-shoulders glyph inside a circular ink outline. */
@Composable
private fun PersonIcon(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(colors.background)
            .border(width = 2.5.dp, color = colors.borderStrong, shape = CircleShape),
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val stroke = 2.dp.toPx()
            val headRadius = size.minDimension * 0.16f
            val headCenter = Offset(size.width / 2f, size.height * 0.38f)
            drawCircle(
                color = colors.borderStrong,
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = stroke),
            )
            val shouldersWidth = size.width * 0.5f
            val shouldersLeft = (size.width - shouldersWidth) / 2f
            val shouldersTop = size.height * 0.6f
            drawArc(
                color = colors.borderStrong,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(shouldersLeft, shouldersTop),
                size = androidx.compose.ui.geometry.Size(shouldersWidth, size.height * 0.34f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun ReceiverCardPreview() {
    PaczkofastTheme {
        ReceiverCard(
            name = "Alex Example",
            phone = "+48 500 100 200",
            isYou = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}
