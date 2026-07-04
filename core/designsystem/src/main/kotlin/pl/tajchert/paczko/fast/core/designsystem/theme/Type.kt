package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.R

/**
 * Typography for the Paczkofast app.
 *
 * The design uses two families:
 * - **Space Mono** — metadata / mono captions and codes (weights 400/700)
 * - **Space Grotesk** — display text: wordmark, screen titles, buttons,
 *   pickup deadlines and codes, plus body copy
 *
 * Space Grotesk is bundled as a variable font; specific weights are
 * instantiated via [FontVariation.Settings]. Space Mono is bundled as
 * separate weight files (regular/bold).
 */

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val SpaceMonoFamily = FontFamily(
    Font(R.font.space_mono, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold),
)

val SpaceGroteskFamily = FontFamily(
    variableFont(R.font.space_grotesk, FontWeight.Medium),
    variableFont(R.font.space_grotesk, FontWeight.SemiBold),
    variableFont(R.font.space_grotesk, FontWeight.Bold),
)

// Uppercase tracked mono caption, e.g. "READY TO PICKUP", "LOCKER WAW01A"
val MonoLabel = TextStyle(
    fontFamily = SpaceMonoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 10.5.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.sp,
)

/**
 * Larger [MonoLabel] variant for the locker/meta lines on parcel & history
 * cards, where the 10.5sp caption reads too small next to the card title.
 */
val MonoLabelLarge = MonoLabel.copy(
    fontSize = 12.sp,
    lineHeight = 15.sp,
)

val PaczkofastTypography = Typography(
    // Display styles — Space Grotesk, large impactful text
    displayLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp,
    ),
    // e.g. "You're 8 m away" headline of the open-box flow
    displaySmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // Headline styles — Space Grotesk bold, e.g. sender name on detail
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),

    // Title styles — Space Grotesk for app/screen titles and deadlines
    // App wordmark "Paczkofast"
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    ),
    // Screen titles ("Parcel details"), countdown numbers
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    // "Pick up by Fri 3 Jul, 12:56"
    titleSmall = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
    ),

    // Body styles — Space Grotesk (disclaimer text and similar body copy)
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp,
    ),
    // Card metadata lines ("Locker WAW01A · Example street 12 · 350 m") — Space Mono
    bodySmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp,
    ),

    // Label styles
    // Primary action buttons ("Open box remotely") — Space Grotesk
    labelLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    // Emphasised small labels ("46 h left", count badges) — Space Mono
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    // Uppercase section labels ("READY FOR PICKUP") — Space Mono
    labelSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    ),
)
