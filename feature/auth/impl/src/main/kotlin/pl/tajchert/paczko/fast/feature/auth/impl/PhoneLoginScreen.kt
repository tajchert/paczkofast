package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.NumericKeypad
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Login step 1 — phone number entry with the in-app keypad (design 4a).
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
                LogoBadge()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Log in to Paczkofast",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.4).sp,
                        ),
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    Text(
                        text = "We'll text you a 6-digit code.\nNo passwords, ever.",
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
                    style = MaterialTheme.typography.labelSmall,
                    color = PaczkofastTheme.colors.textMuted,
                )
                PhoneField(phoneDigits = state.phoneDigits)
                TermsLine()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LoginPrimaryButton(
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

/** Amber app-logo badge: rounded square with the parcel "tape band". */
@Composable
private fun LogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .background(PaczkofastTheme.colors.accent, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 4.dp)
                .background(PaczkofastTheme.colors.onAccent, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun PhoneField(
    phoneDigits: String,
    modifier: Modifier = Modifier,
) {
    val numberStyle = MaterialTheme.typography.titleMedium.copy(
        fontSize = 20.sp,
        lineHeight = 26.sp,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PaczkofastTheme.colors.cardSurface, RoundedCornerShape(16.dp))
            .border(1.5.dp, PaczkofastTheme.colors.accent, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "+48",
            style = numberStyle,
            color = PaczkofastTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(width = 1.dp, height = 24.dp)
                .background(PaczkofastTheme.colors.sizeBadgeBorder),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatPhoneDigits(phoneDigits),
            style = numberStyle.copy(letterSpacing = 1.sp),
            color = PaczkofastTheme.colors.textPrimary,
        )
        BlinkingCursor(
            height = 22.dp,
            modifier = Modifier.padding(start = 1.dp),
        )
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
            state = AuthUiState(phoneDigits = "6014803"),
            onDigit = {},
            onBackspace = {},
            onSendCode = {},
        )
    }
}
