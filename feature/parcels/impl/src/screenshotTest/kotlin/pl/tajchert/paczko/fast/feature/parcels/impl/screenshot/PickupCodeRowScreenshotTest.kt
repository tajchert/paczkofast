package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ExpandablePickupCodeRow

// Obviously-fake pickup code/QR per the repo's public-safety rules.
private const val SAMPLE_OPEN_CODE = "000000"
private const val SAMPLE_QR = "P|000000|000000000000000000000000"

@PreviewTest
@PaczkofastPreviews
@Composable
private fun PickupCodeRowCollapsed() {
    PaczkofastTheme {
        ExpandablePickupCodeRow(
            qrCode = SAMPLE_QR,
            openCode = SAMPLE_OPEN_CODE,
            modifier = Modifier.padding(16.dp),
            initiallyExpanded = false,
        )
    }
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun PickupCodeRowExpanded() {
    PaczkofastTheme {
        ExpandablePickupCodeRow(
            qrCode = SAMPLE_QR,
            openCode = SAMPLE_OPEN_CODE,
            modifier = Modifier.padding(16.dp),
            initiallyExpanded = true,
        )
    }
}
