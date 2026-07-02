package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.NumericKeypad
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Login step 2 — 6-digit SMS code entry with resend countdown (design 4b).
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .offset(x = (-12).dp),
        ) {
            IconButton(onClick = onBackToPhone) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PaczkofastTheme.colors.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enter the code",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        letterSpacing = (-0.4).sp,
                    ),
                    color = PaczkofastTheme.colors.textPrimary,
                )
                SentToLine(
                    phoneDigits = state.phoneDigits,
                    onWrongNumber = onBackToPhone,
                )
            }

            CodeBoxes(codeDigits = state.codeDigits)

            AutofillHintCard()

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LoginPrimaryButton(
                    text = "Log in",
                    onClick = onConfirm,
                    enabled = state.canConfirmCode,
                    isLoading = state.isLoading,
                )
                ResendLine(
                    secondsLeft = state.resendSecondsLeft,
                    onResend = onResend,
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
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun SentToLine(
    phoneDigits: String,
    onWrongNumber: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            append("Sent by SMS to ")
            withStyle(
                SpanStyle(
                    color = PaczkofastTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                append("+48 ${formatPhoneDigits(phoneDigits)}")
            }
            append(" · ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "wrong-number",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = PaczkofastTheme.colors.textSecondary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                ) { onWrongNumber() },
            ) {
                append("wrong number?")
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

@Composable
private fun CodeBoxes(
    codeDigits: String,
    modifier: Modifier = Modifier,
) {
    val boxShape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(AuthViewModel.CODE_LENGTH) { index ->
            val isActive = index == codeDigits.length
            val isFilled = index < codeDigits.length
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(
                        color = if (isFilled || isActive) {
                            PaczkofastTheme.colors.cardSurface
                        } else {
                            PaczkofastTheme.colors.cardSurfaceSubtle
                        },
                        shape = boxShape,
                    )
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = when {
                            isActive -> PaczkofastTheme.colors.accent
                            isFilled -> PaczkofastTheme.colors.sizeBadgeBorder
                            else -> PaczkofastTheme.colors.cardBorderSubtle
                        },
                        shape = boxShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    isFilled -> Text(
                        text = codeDigits[index].toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    isActive -> BlinkingCursor(height = 26.dp)
                }
            }
        }
    }
}

@Composable
private fun AutofillHintCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PaczkofastTheme.colors.cardSurfaceSubtle, shape)
            .border(1.dp, PaczkofastTheme.colors.cardBorderSubtle, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val tint = PaczkofastTheme.colors.textMuted
        val icon = androidx.compose.runtime.remember(tint) { smsDocumentIcon(tint) }
        Icon(
            painter = rememberVectorPainter(icon),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = "Fills in automatically when the SMS arrives",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = PaczkofastTheme.colors.textMuted,
        )
    }
}

@Composable
private fun ResendLine(
    secondsLeft: Int,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (secondsLeft > 0) {
        Text(
            text = buildAnnotatedString {
                append("Resend code in ")
                withStyle(
                    SpanStyle(
                        color = PaczkofastTheme.colors.textSecondary,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append("0:%02d".format(secondsLeft))
                }
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = PaczkofastTheme.colors.textFaint,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth(),
        )
    } else {
        Text(
            text = "Resend code",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            ),
            color = PaczkofastTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onResend),
        )
    }
}

/** Small outlined SMS/message icon from the design's autofill hint. */
private fun smsDocumentIcon(tint: androidx.compose.ui.graphics.Color): ImageVector =
    ImageVector.Builder(
        name = "SmsDocument",
        defaultWidth = 17.dp,
        defaultHeight = 17.dp,
        viewportWidth = 20f,
        viewportHeight = 20f,
    ).apply {
        path(stroke = SolidColor(tint), strokeLineWidth = 1.5f) {
            // rounded rect x=3 y=2.5 w=14 h=15 rx=3
            moveTo(6f, 2.5f)
            lineTo(14f, 2.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 17f, 5.5f)
            lineTo(17f, 14.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14f, 17.5f)
            lineTo(6f, 17.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3f, 14.5f)
            lineTo(3f, 5.5f)
            arcTo(3f, 3f, 0f, isMoreThanHalf = false, isPositiveArc = true, 6f, 2.5f)
            close()
        }
        path(
            stroke = SolidColor(tint),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(7f, 6.5f)
            lineTo(13f, 6.5f)
            moveTo(7f, 10f)
            lineTo(13f, 10f)
        }
    }.build()

@PaczkofastPreviews
@Composable
private fun OtpScreenPreview() {
    PaczkofastTheme {
        OtpScreen(
            state = AuthUiState(
                step = AuthStep.Otp,
                phoneDigits = "601480312",
                codeDigits = "4179",
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
