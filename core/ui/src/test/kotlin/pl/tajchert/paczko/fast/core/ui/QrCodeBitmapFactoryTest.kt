package pl.tajchert.paczko.fast.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeBitmapFactoryTest {
    @Test
    fun createsSquareBitmapForQrPayload() {
        val bitmap = QrCodeBitmapFactory.create("opaque-qr")

        // Rendered at native module resolution: a small square (one pixel per
        // module plus quiet zone), upscaled for display by QrCodeImage.
        assertEquals(bitmap.width, bitmap.height)
        assertTrue(bitmap.width in 1..128)
        val backgroundPixel = bitmap.getPixel(0, 0)
        val hasContrastingPixel = (0 until bitmap.width).any { x ->
            (0 until bitmap.height).any { y ->
                bitmap.getPixel(x, y) != backgroundPixel
            }
        }
        assertTrue(hasContrastingPixel)
    }
}
