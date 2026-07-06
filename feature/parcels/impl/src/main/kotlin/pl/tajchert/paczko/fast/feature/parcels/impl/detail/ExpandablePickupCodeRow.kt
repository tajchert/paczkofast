package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.impl.R

/**
 * Expandable row shown once a parcel/box has been picked up. Collapsed by
 * default; tapping reveals the stored pickup [qrCode] + [openCode] as a
 * read-only reference. After pickup the compartment is already emptied, so this
 * is a receipt — there is no collect/open action here.
 *
 * Callers gate rendering on code presence: only show this row when [qrCode] or
 * [openCode] is non-blank.
 *
 * [initiallyExpanded] exists only so screenshot previews can capture the
 * expanded state (screenshot tests cannot tap); real callers omit it.
 */
@Composable
internal fun ExpandablePickupCodeRow(
    qrCode: String?,
    openCode: String?,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    val colors = PaczkofastTheme.colors
    var expanded by rememberSaveable(initiallyExpanded) { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "pickupCodeChevron",
    )
    val stateLabel = stringResource(
        if (expanded) R.string.hide_pickup_code else R.string.show_pickup_code,
    )

    PaczkofastCard(
        modifier = modifier.semantics { stateDescription = stateLabel },
        onClick = { expanded = !expanded },
        onClickLabel = stateLabel,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.background, RoundedCornerShape(7.dp))
                    .border(2.5.dp, colors.borderStrong, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(15.dp),
                )
            }
            Text(
                text = stringResource(R.string.pickup_code_qr),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.rotate(chevronRotation),
            )
        }

        if (expanded) {
            when {
                !qrCode.isNullOrBlank() -> QrPanel(
                    payload = qrCode,
                    modifier = Modifier.padding(top = 12.dp),
                    code = openCode,
                    qrSize = 150,
                )
                !openCode.isNullOrBlank() -> Text(
                    text = openCode,
                    style = MonoLabelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}
