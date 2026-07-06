package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Slim top bar for detail screens: neo-brutalist back button chip,
 * left-aligned Space Grotesk title and optional trailing actions (e.g.
 * overflow menu).
 */
@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Reserve the back-chip footprint even when hidden, so the bar's height (and thus any
        // Scaffold content padding below it) stays identical whether or not the control shows.
        if (showBackButton) {
            BackButtonChip(onClick = onBack)
        } else {
            Spacer(modifier = Modifier.size(34.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = PaczkofastTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/**
 * Neo-brutalist back button: a 34dp white chip with a hard ink border and
 * offset shadow, containing a tinted chevron. Shared by [DetailTopBar] and
 * [PaczkofastTopAppBar].
 */
@Composable
internal fun BackButtonChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    contentDescription: String = "Back",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    NeoSurface(
        modifier = modifier
            .size(34.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(11.dp),
        fill = PaczkofastTheme.colors.headerIconBackground,
        borderColor = PaczkofastTheme.colors.borderStrong,
        borderWidth = 2.5.dp,
        shadowOffset = 2.dp,
        pressed = pressed,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PaczkofastTheme.colors.textPrimary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(18.dp),
        )
    }
}

/**
 * Overflow ("more") action for [DetailTopBar].
 */
@Composable
fun OverflowAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options",
            tint = PaczkofastTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun DetailTopBarPreview() {
    PaczkofastTheme {
        DetailTopBar(
            title = "Parcel details",
            onBack = {},
            actions = { OverflowAction(onClick = {}) },
        )
    }
}
