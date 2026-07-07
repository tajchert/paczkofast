package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Height reserved for the bottom action zone. Sized for the tallest case — a primary
 * button stacked above a *holding* override bar (which grows a "Keep holding…" progress
 * track). Because the zone height is fixed and its content is bottom-aligned, the primary
 * action's baseline is invariant whether the zone holds 0, 1, or 2 controls.
 */
private val ActionZoneHeight = 200.dp

/**
 * Fixed three-zone collect layout with structurally anchored positions:
 *
 *  - **Hero** (216.dp cross-fade box) is top-anchored at a fixed offset below the top bar —
 *    it is *not* centered in a flexible region, so its absolute Y (and center) is identical
 *    in every state.
 *  - **Caption** ([header]) is a mono line directly under the hero (the locker/box label,
 *    design 5a). It keeps a reserved fixed-height slot so present/blank never shifts.
 *  - **Headline** renders under the caption. **Subline** keeps a reserved fixed-height,
 *    single-line slot.
 *  - **Detail** renders in a flexible `weight(1f)` region that absorbs all remaining space
 *    between the fixed top block and the fixed-height action zone. A detail appearing or
 *    growing therefore never moves the hero above it nor the action baseline below it.
 *  - **Action** sits in a fixed-height, bottom-aligned zone at the very bottom.
 *
 * Net guarantee: hero center, caption, and the primary-action baseline have identical Y
 * across idle → holding → opening → box open → success → error, and when the subline or
 * detail appears or the distance updates.
 *
 * @param header Mono caption rendered *under* the hero (e.g. "LOCKER WAW01A").
 */
@Composable
fun CollectScaffold(
    header: String,
    hero: @Composable (CollectHero) -> Unit,
    heroKey: CollectHero,
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
        // --- Fixed top block: small gap → hero → caption → headline → reserved subline. ---
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(216.dp),
            contentAlignment = Alignment.Center,
        ) {
            Crossfade(targetState = heroKey, label = "collect-hero") { targetHero ->
                hero(targetHero)
            }
        }
        // Reserved caption slot under the hero (design order: ring → LOCKER label → headline).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(top = 20.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = header,
                style = MonoLabel,
                color = colors.monoLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = headline,
            style = MaterialTheme.typography.displaySmall,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp),
        )
        // Reserved single-line slot: whether the subline is present or absent, nothing below
        // it in the top block shifts.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(top = 6.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (subline != null) {
                Text(
                    text = subline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // --- Flexible middle: detail lives here and absorbs the slack, so it can never
        // push the fixed hero above it or the fixed action zone below it. The slot itself
        // stays weight(1f) (fixed footprint); only its CONTENT scrolls, so a tall detail
        // (multi-package checklist / wrapping ErrorDetail at large font scale) scrolls
        // instead of clipping — the hero/header/subline above and action zone below don't move.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 14.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (detail != null) detail()
        }
        // --- Fixed-height, bottom-aligned action zone. ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ActionZoneHeight)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            action()
        }
    }
}
