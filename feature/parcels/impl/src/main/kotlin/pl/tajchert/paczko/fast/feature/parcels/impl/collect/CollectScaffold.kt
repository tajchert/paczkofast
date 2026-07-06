package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Fixed three-zone collect layout: header (top), a 216.dp centered hero that cross-fades
 * its glyph in place, headline+subline+detail below it, and a bottom action zone. Every
 * zone is always laid out so state changes swap content without moving anchors.
 */
@Composable
fun CollectScaffold(
    header: String,
    hero: @Composable () -> Unit,
    heroKey: Any,
    headline: String,
    subline: String?,
    detail: (@Composable () -> Unit)?,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = header,
            style = MonoLabel,
            color = colors.monoLabel,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(216.dp),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = heroKey, label = "collect-hero") { _ -> hero() }
            }
            Text(
                text = headline,
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp),
            )
            Box(
                modifier = Modifier.height(24.dp).padding(top = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (subline != null) {
                    Text(
                        text = subline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (detail != null) {
                Box(modifier = Modifier.padding(top = 18.dp)) { detail() }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            action()
        }
    }
}
