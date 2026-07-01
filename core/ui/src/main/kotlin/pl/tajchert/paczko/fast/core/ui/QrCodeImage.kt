package pl.tajchert.paczko.fast.core.ui

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeBitmapFactory {
    fun create(payload: String, sizePx: Int): Bitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}

@Composable
fun QrCodeImage(
    payload: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 768,
) {
    val bitmap = remember(payload, sizePx) { QrCodeBitmapFactory.create(payload, sizePx) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Pickup QR code",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
