package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val LockerInfoCardShape = RoundedCornerShape(18.dp)
private val FlagPillShape = RoundedCornerShape(6.dp)

/**
 * Rich locker info card for the parcel detail screen: a photo strip (or a
 * hatched placeholder when [photoUrl] is unavailable) with an "OPEN 24/7"
 * badge, followed by locker id/type, address, region and a wrapping row of
 * flag pills (e.g. "EASY ACCESS ZONE", "AIR SENSOR"), and a "Navigate"
 * action.
 */
@Composable
fun LockerInfoCard(
    lockerId: String,
    address: String,
    modifier: Modifier = Modifier,
    photoUrl: String? = null,
    isOpen24_7: Boolean = false,
    lockerType: String? = null,
    region: String? = null,
    flags: List<String> = emptyList(),
    onNavigate: () -> Unit = {},
) {
    val colors = PaczkofastTheme.colors
    NeoSurface(
        modifier = modifier.fillMaxWidth(),
        shape = LockerInfoCardShape,
        fill = colors.cardSurface,
        borderColor = colors.borderStrong,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                if (photoUrl == null) {
                    HatchedPlaceholder(modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(FlagPillShape)
                            .background(colors.cardSurface)
                            .border(width = 2.dp, color = colors.borderStrong, shape = FlagPillShape),
                    ) {
                        Text(
                            text = "LOCKER PHOTO",
                            style = MonoLabel,
                            color = colors.textMuted,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
                // TODO: render the real photo from photoUrl once image loading is wired up.
                if (isOpen24_7) {
                    StatusChip(
                        text = "Open 24/7",
                        style = StatusChipStyle.Accent,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val monoLine = buildString {
                        append("LOCKER ")
                        append(lockerId)
                        if (lockerType != null) {
                            append(" · ")
                            append(lockerType)
                        }
                    }
                    Text(
                        text = monoLine,
                        style = MonoLabel,
                        color = colors.textMuted,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .clip(FlagPillShape)
                            .border(width = 2.dp, color = colors.borderStrong, shape = FlagPillShape),
                    ) {
                        Text(
                            text = "LOCKER",
                            style = MonoLabel,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.textPrimary,
                    )
                    region?.let {
                        Text(
                            text = it.uppercase(),
                            style = MonoLabel,
                            color = colors.textMuted,
                        )
                    }
                }
                if (flags.isNotEmpty()) {
                    FlagPillRow(flags = flags)
                }
                PaczkofastOutlinedButton(
                    onClick = onNavigate,
                    text = "Navigate",
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                )
            }
        }
    }
}

/** Wrapping row of flag pills, e.g. "EASY ACCESS ZONE" / "AIR SENSOR". */
@Composable
private fun FlagPillRow(flags: List<String>, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        flags.forEach { flag ->
            Box(
                modifier = Modifier
                    .clip(FlagPillShape)
                    .background(colors.background)
                    .border(width = 2.dp, color = colors.borderStrong, shape = FlagPillShape),
            ) {
                Text(
                    text = flag.uppercase(),
                    style = MonoLabel,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
    }
}

/** Diagonal-stripe placeholder shown when no locker photo is available. */
@Composable
private fun HatchedPlaceholder(modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Canvas(modifier = modifier.background(colors.background)) {
        val stripeGap = 20.dp.toPx()
        val stroke = 10.dp.toPx()
        val diagonal = size.width + size.height
        var offset = -size.height
        while (offset < diagonal) {
            drawLine(
                color = colors.cardSurfaceSubtle,
                start = Offset(offset, size.height),
                end = Offset(offset + size.height, 0f),
                strokeWidth = stroke,
                cap = StrokeCap.Butt,
            )
            offset += stripeGap
        }
    }
}

@PaczkofastPreviews
@Composable
private fun LockerInfoCardPreview() {
    PaczkofastTheme {
        LockerInfoCard(
            lockerId = "WAW01A",
            lockerType = "IN A SERVICE PREMISE",
            address = "Example street 12, 00-000 Example City",
            region = "MAZOWIECKIE · POLAND",
            isOpen24_7 = true,
            flags = listOf("EASY ACCESS ZONE", "AIR SENSOR"),
            onNavigate = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
