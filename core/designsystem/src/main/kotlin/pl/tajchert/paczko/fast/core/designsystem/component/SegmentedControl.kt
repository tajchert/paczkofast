package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Horizontal segmented control — a track holding equal-width pills, the
 * selected one filled amber. Used for the theme picker (System / Light / Dark).
 *
 * @param options Ordered list of (value, label) pairs.
 * @param selected The currently selected value.
 */
@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .clip(RoundedCornerShape(12.dp))
            .background(PaczkofastTheme.colors.background)
            .border(2.5.dp, PaczkofastTheme.colors.borderStrong, RoundedCornerShape(12.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { (value, label) ->
            SegmentedItem(
                label = label,
                selected = value == selected,
                onClick = { onSelect(value) },
            )
        }
    }
}

@Composable
private fun RowScope.SegmentedItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PaczkofastTheme.colors.accent else Color.Transparent)
            .then(
                if (selected) {
                    Modifier.border(2.dp, PaczkofastTheme.colors.borderStrong, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                },
            )
            .selectable(
                selected = selected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics {
                contentDescription = label
                stateDescription = if (selected) "Selected" else "Not selected"
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) {
                PaczkofastTheme.colors.textPrimary
            } else {
                PaczkofastTheme.colors.textMuted
            },
        )
    }
}

@PaczkofastPreviews
@Composable
private fun SegmentedControlPreview() {
    PaczkofastTheme {
        SegmentedControl(
            options = listOf("system" to "System", "light" to "Light", "dark" to "Dark"),
            selected = "light",
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
