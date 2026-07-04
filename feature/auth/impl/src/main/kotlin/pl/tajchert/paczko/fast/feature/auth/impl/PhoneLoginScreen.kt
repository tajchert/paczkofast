package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.NeoSurface
import pl.tajchert.paczko.fast.core.designsystem.component.NumericKeypad
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Login step 1 — phone number entry with the in-app keypad (design 2e).
 */
@Composable
fun PhoneLoginScreen(
    state: AuthUiState,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSendCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaczkofastTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                LogoTile()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Paczkofast",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.4).sp,
                        ),
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    Text(
                        text = "Log in with the phone number your parcels are sent to. " +
                            "We'll text you a code.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp,
                        ),
                        color = PaczkofastTheme.colors.textMuted,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PHONE NUMBER",
                    style = MonoLabel,
                    color = PaczkofastTheme.colors.textMuted,
                )
                PhoneInputRow(phoneDigits = state.phoneDigits)
                TermsLine()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PaczkofastButton(
                    text = "Send code",
                    onClick = onSendCode,
                    enabled = state.canSendCode,
                    isLoading = state.isLoading,
                )
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        NumericKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

/** 72dp yellow neo-brutalist logo tile with a rotated ink diamond. */
@Composable
private fun LogoTile(modifier: Modifier = Modifier) {
    NeoSurface(
        modifier = modifier.size(72.dp),
        shape = RoundedCornerShape(22.dp),
        fill = PaczkofastTheme.colors.accent,
        borderColor = PaczkofastTheme.colors.borderStrong,
        shadowOffset = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(26.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(7.dp))
                .background(PaczkofastTheme.colors.borderStrong),
        )
    }
}

/** "+48" prefix chip + the national-number field, both neo-brutalist surfaces. */
@Composable
private fun PhoneInputRow(
    phoneDigits: String,
    modifier: Modifier = Modifier,
) {
    val numberStyle = MaterialTheme.typography.labelMedium
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NeoSurface(
            modifier = Modifier
                .width(78.dp)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            fill = PaczkofastTheme.colors.cardSurface,
            borderColor = PaczkofastTheme.colors.borderStrong,
            shadowOffset = 3.dp,
        ) {
            Text(
                text = "+48",
                style = numberStyle.copy(fontSize = 16.sp),
                color = PaczkofastTheme.colors.textPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        NeoSurface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            fill = PaczkofastTheme.colors.cardSurface,
            borderColor = PaczkofastTheme.colors.borderStrong,
            shadowOffset = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatPhoneDigits(phoneDigits),
                    style = numberStyle.copy(fontSize = 18.sp, letterSpacing = 1.sp),
                    color = PaczkofastTheme.colors.textPrimary,
                )
                BlinkingCursor(
                    height = 22.dp,
                    modifier = Modifier.padding(start = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun TermsLine(modifier: Modifier = Modifier) {
    val linkStyle = SpanStyle(
        color = PaczkofastTheme.colors.textMuted,
        textDecoration = TextDecoration.Underline,
    )
    Text(
        text = buildAnnotatedString {
            append("By continuing you accept the ")
            withStyle(linkStyle) { append("Terms") }
            append(" and ")
            withStyle(linkStyle) { append("Privacy policy") }
        },
        style = MaterialTheme.typography.bodySmall,
        color = PaczkofastTheme.colors.textFaint,
        modifier = modifier,
    )
}

@PaczkofastPreviews
@Composable
private fun PhoneLoginScreenPreview() {
    PaczkofastTheme {
        PhoneLoginScreen(
            state = AuthUiState(phoneDigits = "500100"),
            onDigit = {},
            onBackspace = {},
            onSendCode = {},
        )
    }
}
