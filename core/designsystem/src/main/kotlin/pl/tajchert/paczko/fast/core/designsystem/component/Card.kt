package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Standard neo-brutalist card component for the Paczkofast app.
 *
 * A white [NeoSurface] with a thick ink border and hard offset shadow, 18dp
 * corner radius. When [onClick] is provided the card presses into its own
 * shadow on tap (see [NeoSurface]'s `pressed` behavior).
 *
 * ## Usage
 *
 * ```kotlin
 * PaczkofastCard(
 *     onClick = { navigateToDetail(parcel.id) },
 * ) {
 *     Text(parcel.title)
 *     Text(parcel.status)
 * }
 * ```
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler. If provided, the card becomes clickable
 *   and animates toward its shadow while pressed.
 * @param content The card content
 */
@Composable
fun PaczkofastCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = onClick != null && interactionSource.collectIsPressedAsState().value

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(18.dp),
        fill = PaczkofastTheme.colors.cardSurface,
        borderColor = PaczkofastTheme.colors.borderStrong,
        pressed = pressed,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content,
        )
    }
}

/**
 * More prominent card variant — same neo-brutalist surface with a deeper
 * hard shadow to draw extra attention (e.g. featured/expanded content).
 */
@Composable
fun PaczkofastElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = onClick != null && interactionSource.collectIsPressedAsState().value

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(18.dp),
        fill = PaczkofastTheme.colors.cardSurface,
        borderColor = PaczkofastTheme.colors.borderStrong,
        shadowOffset = 4.dp,
        pressed = pressed,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content,
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@PaczkofastPreviews
@Composable
private fun PaczkofastCardPreview() {
    PaczkofastTheme {
        PaczkofastCard(
            modifier = Modifier.padding(16.dp),
            onClick = {},
        ) {
            Text(
                text = "Card Title",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This is the card content with some description text.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastElevatedCardPreview() {
    PaczkofastTheme {
        PaczkofastElevatedCard(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Elevated Card",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This card has a deeper shadow for more prominence.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
