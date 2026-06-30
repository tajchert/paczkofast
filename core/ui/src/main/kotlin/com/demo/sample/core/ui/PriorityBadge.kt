package com.demo.sample.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.sample.core.designsystem.theme.PriorityHigh
import com.demo.sample.core.designsystem.theme.PriorityLow
import com.demo.sample.core.designsystem.theme.PriorityMedium
import com.demo.sample.core.designsystem.theme.SampleTheme
import com.demo.sample.core.model.TaskPriority

/**
 * Badge showing task priority with color and text.
 *
 * ## Color Coding
 *
 * - HIGH: Red - demands attention
 * - MEDIUM: Orange - moderate urgency
 * - LOW: Green - can wait
 *
 * @param priority The priority level to display
 * @param modifier Modifier for the badge
 */
@Composable
fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, contentColor, label) = when (priority) {
        TaskPriority.HIGH -> Triple(
            PriorityHigh.copy(alpha = 0.15f),
            PriorityHigh,
            "High",
        )
        TaskPriority.MEDIUM -> Triple(
            PriorityMedium.copy(alpha = 0.15f),
            PriorityMedium,
            "Medium",
        )
        TaskPriority.LOW -> Triple(
            PriorityLow.copy(alpha = 0.15f),
            PriorityLow,
            "Low",
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun PriorityBadgeHighPreview() {
    SampleTheme {
        PriorityBadge(
            priority = TaskPriority.HIGH,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriorityBadgeMediumPreview() {
    SampleTheme {
        PriorityBadge(
            priority = TaskPriority.MEDIUM,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PriorityBadgeLowPreview() {
    SampleTheme {
        PriorityBadge(
            priority = TaskPriority.LOW,
            modifier = Modifier.padding(8.dp),
        )
    }
}
