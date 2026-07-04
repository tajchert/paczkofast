package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class HardShadowTest {
    @Test fun press_collapses_by_shadow_offset() {
        assertEquals(3.dp, pressTranslation(pressed = true, offset = 3.dp))
    }
    @Test fun idle_has_no_translation() {
        assertEquals(0.dp, pressTranslation(pressed = false, offset = 3.dp))
    }
}
