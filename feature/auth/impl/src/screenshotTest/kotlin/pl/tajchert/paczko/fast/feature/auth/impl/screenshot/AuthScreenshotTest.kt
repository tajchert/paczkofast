package pl.tajchert.paczko.fast.feature.auth.impl.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.feature.auth.impl.AuthStep
import pl.tajchert.paczko.fast.feature.auth.impl.AuthUiState
import pl.tajchert.paczko.fast.feature.auth.impl.DISCLAIMER_PAGE
import pl.tajchert.paczko.fast.feature.auth.impl.DisclaimerPage
import pl.tajchert.paczko.fast.feature.auth.impl.OnboardingFooter
import pl.tajchert.paczko.fast.feature.auth.impl.OtpScreen
import pl.tajchert.paczko.fast.feature.auth.impl.PAGE_COUNT
import pl.tajchert.paczko.fast.feature.auth.impl.PhoneLoginScreen
import pl.tajchert.paczko.fast.feature.auth.impl.WELCOME_PAGE
import pl.tajchert.paczko.fast.feature.auth.impl.WelcomePage

@PreviewTest
@PaczkofastPreviews
@Composable
private fun PhoneLoginScreenshot() {
    PaczkofastTheme {
        PhoneLoginScreen(
            state = AuthUiState(phoneDigits = "500100200"),
            onPhoneChange = {},
            onSendCode = {},
        )
    }
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun OtpScreenshot() {
    PaczkofastTheme {
        OtpScreen(
            state = AuthUiState(
                step = AuthStep.Otp,
                phoneDigits = "500100200",
                codeDigits = "000000",
                resendSecondsLeft = 24,
            ),
            onCodeChange = {},
            onConfirm = {},
            onResend = {},
            onBackToPhone = {},
        )
    }
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun OnboardingWelcomePageScreenshot() {
    PaczkofastTheme {
        Column(modifier = Modifier.background(PaczkofastTheme.colors.background)) {
            WelcomePage(modifier = Modifier.fillMaxWidth())
            OnboardingFooter(
                pagerState = rememberPagerState(initialPage = WELCOME_PAGE, pageCount = { PAGE_COUNT }),
                onContinueClicked = {},
                onFinishClicked = {},
            )
        }
    }
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun OnboardingDisclaimerPageScreenshot() {
    PaczkofastTheme {
        Column(modifier = Modifier.background(PaczkofastTheme.colors.background)) {
            DisclaimerPage(modifier = Modifier.fillMaxWidth())
            OnboardingFooter(
                pagerState = rememberPagerState(initialPage = DISCLAIMER_PAGE, pageCount = { PAGE_COUNT }),
                onContinueClicked = {},
                onFinishClicked = {},
            )
        }
    }
}
