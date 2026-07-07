package pl.tajchert.paczko.fast.feature.auth.impl.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF0E0E10,
    locale = "en",
)
@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFDFAEF,
    locale = "en",
)
annotation class EnglishPaczkofastPreviews

@Composable
internal fun EnglishScreenshotContent(content: @Composable () -> Unit) {
    remember { Locale.setDefault(Locale.ENGLISH) }
    content()
}
