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
        val bitmap = QrCodeBitmapFactory.create("opaque-qr", sizePx = 256)

        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
        val backgroundPixel = bitmap.getPixel(0, 0)
        val hasContrastingPixel = (0 until bitmap.width).any { x ->
            (0 until bitmap.height).any { y ->
                bitmap.getPixel(x, y) != backgroundPixel
            }
        }
        assertTrue(hasContrastingPixel)
    }
}
