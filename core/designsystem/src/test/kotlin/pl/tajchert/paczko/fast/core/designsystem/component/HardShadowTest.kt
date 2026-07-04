package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HardShadowTest {
    // Pressed state slides the CONTENT toward the shadow (which is always drawn at the
    // full shadowOffset), leaving a 1dp visible sliver — not a full collapse to 0.
    @Test fun press_slides_content_toward_shadow_leaving_1dp_gap() {
        assertEquals(2.dp, pressTranslation(pressed = true, offset = 3.dp))
    }
    @Test fun idle_has_no_translation() {
        assertEquals(0.dp, pressTranslation(pressed = false, offset = 3.dp))
    }
}
