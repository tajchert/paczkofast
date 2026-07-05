package pl.tajchert.paczko.fast.core.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

object QrCodeBitmapFactory {
    /**
     * Builds the QR at its native module resolution — one bitmap pixel per QR
     * module (plus quiet zone), typically well under ~50x50 px. The bitmap is
     * upscaled to its display size by [QrCodeImage] with nearest-neighbor
     * filtering, keeping the modules crisp and high-contrast for locker scanners.
     *
     * Rendering at module resolution (rather than a large fixed pixel size) is
     * what keeps this cheap: a small [Bitmap.setPixels] bulk write instead of
     * hundreds of thousands of per-pixel calls, so it can run inline in
     * composition without blocking the first frame.
     *
     * @param inkColor Color used for the QR "on" modules; the "off" modules always render
     *   white so locker scanners keep reading a dark-on-white code regardless of app theme.
     */
    fun create(payload: String, inkColor: Int = Color.BLACK): Bitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
        // Requesting width/height 1 makes ZXing floor to the QR's native module
        // resolution (it never scales below one pixel per module).
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 1, 1, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                pixels[rowOffset + x] = if (matrix[x, y]) inkColor else Color.WHITE
            }
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}

/**
 * Renders the QR modules in [PaczkofastTheme.colors.qrInk] on white. The ink color is kept dark
 * in both app themes (never inverted) so locker scanners retain contrast.
 *
 * The bitmap is generated at native module resolution and upscaled with
 * [FilterQuality.None] (nearest-neighbor) so the modules stay sharp.
 *
 * Generation runs off the main thread ([Dispatchers.Default]) so the screen
 * paints immediately; the QR appears a frame later. The slot keeps [modifier]'s
 * size while empty, so there is no layout shift.
 */
@Composable
fun QrCodeImage(
    payload: String,
    modifier: Modifier = Modifier,
) {
    val inkColor = PaczkofastTheme.colors.qrInk.toArgb()
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(null, payload, inkColor) {
        value = withContext(Dispatchers.Default) {
            QrCodeBitmapFactory.create(payload, inkColor).asImageBitmap()
        }
    }
    val qr = bitmap
    if (qr != null) {
        Image(
            bitmap = qr,
            contentDescription = "Pickup QR code",
            modifier = modifier,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
    } else {
        // Reserve the QR's footprint while it generates so nothing reflows.
        Box(modifier)
    }
}
