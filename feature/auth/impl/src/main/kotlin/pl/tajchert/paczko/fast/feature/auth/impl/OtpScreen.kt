package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.DetailTopBar
import pl.tajchert.paczko.fast.core.designsystem.component.NeoSurface
import pl.tajchert.paczko.fast.core.designsystem.component.NumericKeypad
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Login step 2 — 6-digit SMS code entry with resend countdown (design 2f).
 */
@Composable
fun OtpScreen(
    state: AuthUiState,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    onResend: () -> Unit,
    onBackToPhone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaczkofastTheme.colors.background)
            .navigationBarsPadding(),
    ) {
        DetailTopBar(title = "Enter code", onBack = onBackToPhone)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SentToLine(phoneDigits = state.phoneDigits)
                CodeBoxes(codeDigits = state.codeDigits)
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PaczkofastButton(
                    text = "Verify",
                    onClick = onConfirm,
                    enabled = state.canConfirmCode,
                    isLoading = state.isLoading,
                )
                ResendChangeRow(
                    secondsLeft = state.resendSecondsLeft,
                    onResend = onResend,
                    onChangeNumber = onBackToPhone,
                )
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        NumericKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 14.dp),
        )
    }
}

@Composable
private fun SentToLine(
    phoneDigits: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            append("We texted a 6-digit code to ")
            withStyle(
                SpanStyle(
                    fontFamily = MonoLabel.fontFamily,
                    fontWeight = FontWeight.Bold,
                    color = PaczkofastTheme.colors.textPrimary,
                ),
            ) {
                append("+48 ${formatPhoneDigits(phoneDigits)}")
            }
        },
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 14.5.sp,
            lineHeight = 22.sp,
        ),
        color = PaczkofastTheme.colors.textMuted,
        modifier = modifier,
    )
}

/**
 * Row of 6 neo-brutalist code cells: filled digits sit on a white surface,
 * the active (next-empty) cell is amber with a hard shadow and an ink
 * caret, and untouched cells stay flush with the cream background.
 */
@Composable
private fun CodeBoxes(
    codeDigits: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(AuthViewModel.CODE_LENGTH) { index ->
            val isActive = index == codeDigits.length
            val isFilled = index < codeDigits.length
            NeoSurface(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                shape = shape,
                fill = when {
                    isActive -> PaczkofastTheme.colors.accent
                    isFilled -> PaczkofastTheme.colors.cardSurface
                    else -> PaczkofastTheme.colors.background
                },
                borderColor = PaczkofastTheme.colors.borderStrong,
                shadow = isActive,
                shadowOffset = 3.dp,
            ) {
                when {
                    isFilled -> Text(
                        text = codeDigits[index].toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 24.sp),
                        color = PaczkofastTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.Center),
                    )

                    isActive -> BlinkingCursor(
                        height = 26.dp,
                        color = PaczkofastTheme.colors.borderStrong,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResendChangeRow(
    secondsLeft: Int,
    onResend: () -> Unit,
    onChangeNumber: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (secondsLeft > 0) {
            Text(
                text = "RESEND IN 0:%02d".format(secondsLeft),
                style = MonoLabel,
                color = PaczkofastTheme.colors.textMuted,
            )
        } else {
            Text(
                text = "RESEND CODE",
                style = MonoLabel.copy(textDecoration = TextDecoration.Underline),
                color = PaczkofastTheme.colors.textPrimary,
                modifier = Modifier.clickable(onClick = onResend),
            )
        }
        Text(
            text = "CHANGE NUMBER",
            style = MonoLabel.copy(textDecoration = TextDecoration.Underline),
            color = PaczkofastTheme.colors.textPrimary,
            modifier = Modifier.clickable(onClick = onChangeNumber),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun OtpScreenPreview() {
    PaczkofastTheme {
        OtpScreen(
            state = AuthUiState(
                step = AuthStep.Otp,
                phoneDigits = "500100200",
                codeDigits = "417",
                resendSecondsLeft = 24,
            ),
            onDigit = {},
            onBackspace = {},
            onConfirm = {},
            onResend = {},
            onBackToPhone = {},
        )
    }
}
