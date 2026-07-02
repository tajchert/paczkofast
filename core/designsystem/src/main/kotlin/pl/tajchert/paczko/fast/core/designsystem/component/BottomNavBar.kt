package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Destinations of the app's bottom navigation.
 */
enum class BottomNavDestination(
    val label: String,
    val icon: ImageVector,
) {
    Parcels("Parcels", Icons.Outlined.Inventory2),
    History("History", Icons.Outlined.History),
    Settings("Settings", Icons.Outlined.Tune),
}

/**
 * Bottom navigation bar with the three app destinations. The selected item
 * is amber, the others faint.
 */
@Composable
fun PaczkofastBottomBar(
    selected: BottomNavDestination,
    onSelect: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PaczkofastTheme.colors.navBorder),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PaczkofastTheme.colors.navBackground)
                .navigationBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 6.dp),
        ) {
            BottomNavDestination.entries.forEach { destination ->
                BottomNavItem(
                    destination = destination,
                    isSelected = destination == selected,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (isSelected) {
        PaczkofastTheme.colors.accentText
    } else {
        PaczkofastTheme.colors.textFaint
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            ),
            color = tint,
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastBottomBarPreview() {
    PaczkofastTheme {
        PaczkofastBottomBar(
            selected = BottomNavDestination.Parcels,
            onSelect = {},
        )
    }
}
