package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.designsystem.component.NeoSurface
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastButton
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private const val PAGE_COUNT = 2
private const val WELCOME_PAGE = 0
private const val DISCLAIMER_PAGE = 1

/**
 * First-launch onboarding: a 2-page pager with a welcome page (7a) and an
 * "unofficial, experimental" disclaimer page (7b). Shown once, gated by
 * `UserPreferences.hasSeenOnboarding` (see [MainActivityViewModel][pl.tajchert.paczko.fast.MainActivityViewModel]).
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaczkofastTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            when (page) {
                WELCOME_PAGE -> WelcomePage()
                else -> DisclaimerPage()
            }
        }

        OnboardingFooter(
            pagerState = pagerState,
            onContinueClicked = {
                scope.launch { pagerState.animateScrollToPage(DISCLAIMER_PAGE) }
            },
            onFinishClicked = {
                viewModel.markSeen()
                onFinished()
            },
        )
    }
}

/** Page 7a — welcome, logo tile + "wind lines", headline and marketing copy. */
@Composable
private fun WelcomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WelcomeLogoTile()
            Column(
                modifier = Modifier.padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                WindLine(width = 40.dp)
                WindLine(width = 68.dp)
                WindLine(width = 40.dp)
            }
        }
        Column(
            modifier = Modifier.padding(top = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome to the fast future",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1).sp,
                ),
                color = PaczkofastTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "One tap to your parcel. No clutter, no digging through menus " +
                    "— the box you need is always right on top.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = PaczkofastTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Page 7b — "fan-made, experimental" disclaimer, badge-tagged card graphic. */
@Composable
private fun DisclaimerPage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UnofficialCardGraphic()
        Column(
            modifier = Modifier.padding(top = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Fan-made, experimental",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1).sp,
                ),
                color = PaczkofastTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Paczkofast is an independent companion app. It is not affiliated " +
                    "with, endorsed by, or related to InPost or any locker operator " +
                    "— and things may occasionally break.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                color = PaczkofastTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 120dp yellow logo tile with a rotated ink diamond (matches [PhoneLoginScreen]'s LogoTile). */
@Composable
private fun WelcomeLogoTile(modifier: Modifier = Modifier) {
    NeoSurface(
        modifier = modifier.size(120.dp),
        shape = RoundedCornerShape(32.dp),
        fill = PaczkofastTheme.colors.accent,
        borderColor = PaczkofastTheme.colors.borderStrong,
        shadowOffset = 5.dp,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(38.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(9.dp))
                .background(PaczkofastTheme.colors.borderStrong),
        )
    }
}

/** One of the three horizontal "speed" bars next to the welcome logo tile. */
@Composable
private fun WindLine(width: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = width, height = 8.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(PaczkofastTheme.colors.borderStrong),
    )
}

/** White NeoSurface diamond with a rotated "UNOFFICIAL" tag above it. */
@Composable
private fun UnofficialCardGraphic(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(130.dp)) {
        NeoSurface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(32.dp),
            fill = PaczkofastTheme.colors.cardSurface,
            borderColor = PaczkofastTheme.colors.borderStrong,
            shadowOffset = 5.dp,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(PaczkofastTheme.colors.background)
                    .border(3.5.dp, PaczkofastTheme.colors.borderStrong, RoundedCornerShape(9.dp)),
            )
        }
        NeoSurface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .rotate(-7f),
            shape = RoundedCornerShape(8.dp),
            fill = PaczkofastTheme.colors.accent,
            borderColor = PaczkofastTheme.colors.borderStrong,
            borderWidth = 2.5.dp,
            shadowOffset = 2.dp,
        ) {
            Text(
                text = "UNOFFICIAL",
                style = MonoLabel,
                color = PaczkofastTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

/** Dot indicator + primary action button, shared across both onboarding pages. */
@Composable
private fun OnboardingFooter(
    pagerState: PagerState,
    onContinueClicked: () -> Unit,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PageDotIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        )
        PaczkofastButton(
            text = if (pagerState.currentPage == WELCOME_PAGE) "Continue" else "I understand — let's go",
            onClick = if (pagerState.currentPage == WELCOME_PAGE) onContinueClicked else onFinishClicked,
        )
    }
}

@Composable
private fun PageDotIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            NeoSurface(
                modifier = Modifier.size(width = if (active) 24.dp else 8.dp, height = 8.dp),
                shape = RoundedCornerShape(5.dp),
                fill = if (active) PaczkofastTheme.colors.accent else PaczkofastTheme.colors.cardSurface,
                borderColor = PaczkofastTheme.colors.borderStrong,
                borderWidth = 2.dp,
                shadow = false,
            ) {}
        }
    }
}

@PaczkofastPreviews
@Composable
private fun OnboardingWelcomePagePreview() {
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

@PaczkofastPreviews
@Composable
private fun OnboardingDisclaimerPagePreview() {
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
