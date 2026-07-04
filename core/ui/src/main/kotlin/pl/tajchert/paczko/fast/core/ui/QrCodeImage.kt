package pl.tajchert.paczko.fast.core.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

object QrCodeBitmapFactory {
    /**
     * @param inkColor Color used for the QR "on" modules; the "off" modules always render
     *   white so locker scanners keep reading a dark-on-white code regardless of app theme.
     */
    fun create(payload: String, sizePx: Int, inkColor: Int = Color.BLACK): Bitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (matrix[x, y]) inkColor else Color.WHITE)
            }
        }
        return bitmap
    }
}

/**
 * Renders the QR modules in [PaczkofastTheme.colors.qrInk] on white. The ink color is kept dark
 * in both app themes (never inverted) so locker scanners retain contrast.
 */
@Composable
fun QrCodeImage(
    payload: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 768,
) {
    val inkColor = PaczkofastTheme.colors.qrInk.toArgb()
    val bitmap = remember(payload, sizePx, inkColor) {
        QrCodeBitmapFactory.create(payload, sizePx, inkColor)
    }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Pickup QR code",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
