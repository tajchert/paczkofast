package com.demo.sample.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.sample.core.designsystem.theme.SampleTheme

/**
 * Standard card component for the Sample app.
 *
 * ## Why Wrap Material Card?
 *
 * 1. **Consistent elevation and shape**: All cards look the same
 * 2. **Standard padding**: Content is always properly padded
 * 3. **Easy theming**: Update all cards from one place
 *
 * ## Usage
 *
 * ```kotlin
 * SampleCard(
 *     onClick = { navigateToDetail(task.id) },
 * ) {
 *     Text(task.title)
 *     Text(task.description)
 * }
 * ```
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler. If provided, the card becomes clickable.
 * @param content The card content
 */
@Composable
fun SampleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

/**
 * Elevated card variant for more prominent content.
 */
@Composable
fun SampleElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun SampleCardPreview() {
    SampleTheme {
        SampleCard(
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

@Preview(showBackground = true)
@Composable
private fun SampleElevatedCardPreview() {
    SampleTheme {
        SampleElevatedCard(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Elevated Card",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This card has higher elevation for more prominence.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
