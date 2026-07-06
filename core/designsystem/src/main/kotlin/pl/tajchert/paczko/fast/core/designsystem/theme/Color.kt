package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Paczkofast app design system.
 *
 * Two fixed brand palettes, aligned to the "App Core Design" mocks:
 * - Dark: cool near-black surfaces (#0E0E10 / #1B1B1F) with warm off-white
 *   foreground (#F2F0E6) and the yellow accent. Neo-brutalist borders/shadows
 *   invert to light on dark, but yellow fills keep an ink border (see
 *   [PaczkofastColors.accentBorder]).
 * - Light: warm paper surfaces with an ink foreground and the same yellow accent.
 *
 * Raw colors below should NOT be used directly in composables. Use either
 * `MaterialTheme.colorScheme` (for Material components) or the app-specific
 * roles on [PaczkofastTheme.colors] (see [PaczkofastColors]).
 */

// Shared brand accent
internal val Amber = Color(0xFFF2AF1D)
internal val OnAmber = Color(0xFF201500)
internal val AmberDeep = Color(0xFFB97F00)

// Neo-brutalist disabled primary button ("faded yellow")
internal val YellowDisabledLight = Color(0xFFF3EAB6)
internal val OnYellowDisabledLight = Color(0xFF9E9678)
internal val YellowDisabledDark = Color(0xFF3A3626)
internal val OnYellowDisabledDark = Color(0xFF8A8266)

// Dark ("Black Amber") palette
internal val Night0 = Color(0xFF0B0A08)
internal val Night1 = Color(0xFF16130F)
internal val Night2 = Color(0xFF121009)
internal val NightNav = Color(0xFF0E0C09)
internal val NightTrack = Color(0xFF262015)
internal val NightBadge = Color(0xFF221D13)
internal val NightRail = Color(0xFF3A3426)
internal val Sand = Color(0xFFF5F0E4)
internal val SandSoft = Color(0xFFC9BFA9)
internal val SandMuted = Color(0xFF93897A)
internal val SandFaint = Color(0xFF6B6353)
internal val UrgentDark = Color(0xFFFF9E2E)
// Multi-package (blue) accent — dark
internal val InfoBlue = Color(0xFF5CA8FF)
internal val NightBlue = Color(0xFF101319)

// Light ("warm paper") palette
internal val Paper0 = Color(0xFFF4F1EA)
internal val Paper1 = Color(0xFFFFFFFF)
internal val Paper2 = Color(0xFFFBF9F3)
internal val PaperBorder = Color(0xFFE9E3D3)
internal val PaperBorderSubtle = Color(0xFFE7E1D2)
internal val PaperBorderBadge = Color(0xFFE4DECE)
internal val PaperTrack = Color(0xFFEFE9DA)
internal val PaperBadge = Color(0xFFE7E1D2)
internal val InkStrong = Color(0xFF211D14)
internal val InkSoft = Color(0xFF655C4B)
internal val InkMuted = Color(0xFF8A8070)
internal val InkFaint = Color(0xFFA79B85)
internal val InkBadge = Color(0xFF7A7264)
internal val PaperChevron = Color(0xFFB8AE99)
internal val TrackDoneLight = Color(0xFFC8BFA8)
internal val TrackActiveLight = Color(0xFFE9A50E)
internal val UrgentLight = Color(0xFFC75B1F)
// Multi-package (blue) accent — light
internal val InfoBlueDeep = Color(0xFF2F6FE0)
internal val PaperBlue = Color(0xFFEDF2FB)

// QR panel (same in both modes — scanners need contrast)
internal val QrInk = Color(0xFF17140E)
internal val QrLabel = Color(0xFF83796A)

// Neo-brutalist — light ("cream")
internal val Cream = Color(0xFFFDFAEF)
internal val CardWhite = Color(0xFFFFFFFF)
internal val Yellow = Color(0xFFFFD400)
internal val Ink = Color(0xFF161511)
internal val InkMutedNb = Color(0xFF5F5B4C)
internal val AlertFill = Color(0xFFFF3B25)
internal val AlertText = Color(0xFFE01507)

// Neo-brutalist — dark ("cool near-black", aligned to "App Core Design Dark")
internal val NightBg = Color(0xFF0E0E10) // screen background + empty progress track
internal val NightCard = Color(0xFF1B1B1F) // cards, nav, badges, elevated rows
internal val NightRingTrack = Color(0xFF2A2A30) // hold-to-open ring, unfilled band
internal val Sand2 = Color(0xFFF2F0E6) // primary text, borders, hard shadows
internal val SandMuted2 = Color(0xFFA5A296) // secondary/muted text
internal val AlertDark = Color(0xFFFF6B58)

// Error palette (kept from Material defaults)
internal val Red40 = Color(0xFFBA1B1B)
internal val Red80 = Color(0xFFFFB4A9)
internal val Red90 = Color(0xFFFFDAD4)
internal val Red10 = Color(0xFF410001)
internal val Red20 = Color(0xFF680003)
internal val Red30 = Color(0xFF930006)

/**
 * App-specific color roles that don't map cleanly onto Material 3's scheme.
 *
 * Access via `PaczkofastTheme.colors` inside a [PaczkofastTheme]:
 *
 * ```kotlin
 * Text(color = PaczkofastTheme.colors.textMuted)
 * ```
 */
@Immutable
data class PaczkofastColors(
    /** Amber brand accent — filled buttons, active progress, counts. */
    val accent: Color,
    /** Content drawn on top of [accent]. */
    val onAccent: Color,
    /** Background of a disabled primary (amber) button. */
    val accentDisabled: Color,
    /** Content drawn on top of [accentDisabled]. */
    val onAccentDisabled: Color,
    /** Amber used as text/icon on the current background (darker in light mode). */
    val accentText: Color,
    /**
     * Border drawn around saturated fills — accent (yellow) buttons/chips/badges
     * and alert (red) blobs. Stays ink in both light and dark so a yellow sticker
     * keeps its hard outline even when neutral borders invert to light on dark.
     */
    val accentBorder: Color,
    /** Urgent "time running out" highlight. */
    val urgent: Color,
    /** Screen background. */
    val background: Color,
    /** Primary text. */
    val textPrimary: Color,
    /** Secondary text (soft). */
    val textSecondary: Color,
    /** Muted text — metadata, captions, section labels. */
    val textMuted: Color,
    /** Faint text/icons — inactive nav, chevrons, done-but-idle track segments. */
    val textFaint: Color,
    /** Prominent card surface (ready-for-pickup cards). */
    val cardSurface: Color,
    /** Border of prominent cards. */
    val cardBorder: Color,
    /** Subtle card surface (in-transit cards). */
    val cardSurfaceSubtle: Color,
    /** Border of subtle cards. */
    val cardBorderSubtle: Color,
    /** Empty part of progress tracks. */
    val trackBackground: Color,
    /** Filled/active part of progress tracks. */
    val trackActive: Color,
    /** Completed-but-not-current track segments. */
    val trackDone: Color,
    /** Unfilled band of the circular hold-to-open progress ring. */
    val ringTrack: Color,
    /** Neutral count badge background. */
    val badgeBackground: Color,
    /** Neutral count badge content. */
    val badgeContent: Color,
    /** Border of the parcel size badge (S/M/L). */
    val sizeBadgeBorder: Color,
    /** Content of the parcel size badge. */
    val sizeBadgeContent: Color,
    /** Bottom navigation background. */
    val navBackground: Color,
    /** Hairline border above bottom navigation. */
    val navBorder: Color,
    /** Status chip background (translucent amber). */
    val statusChipBackground: Color,
    /** Status chip content. */
    val statusChipContent: Color,
    /** QR panel background. */
    val qrPanelBackground: Color,
    /** QR panel border (transparent in dark mode). */
    val qrPanelBorder: Color,
    /** QR modules and pickup code digits. */
    val qrInk: Color,
    /** QR panel caption ("CODE"). */
    val qrLabel: Color,
    /** Border of outlined (secondary) action buttons. */
    val outlineButtonBorder: Color,
    /** Vertical rail connecting timeline events. */
    val timelineRail: Color,
    /** Dot of past (non-current) timeline events. */
    val timelineDotInactive: Color,
    /** Circular icon-button background in the header. */
    val headerIconBackground: Color,
    /** Multi-package (blue) accent — text/icons/fills. */
    val infoAccent: Color,
    /** Content drawn on top of [infoAccent]. */
    val onInfoAccent: Color,
    /** Surface of the multi-package (blue) card. */
    val infoSurface: Color,
    /** Border of the multi-package (blue) card. */
    val infoBorder: Color,
    /** Color of the hard offset drop-shadow behind surfaces. */
    val hardShadow: Color,
    /** Strong 2.5dp surface/border color (ink in light, sand in dark). */
    val borderStrong: Color,
    /** Alert/urgent fill (blobs, urgent progress). */
    val alertFill: Color,
    /** Alert/urgent text. */
    val alertText: Color,
    /** Uppercase mono caption color. */
    val monoLabel: Color,
)

internal val DarkPaczkofastColors = PaczkofastColors(
    accent = Yellow,
    onAccent = Ink,
    accentDisabled = YellowDisabledDark,
    onAccentDisabled = OnYellowDisabledDark,
    accentText = Yellow,
    accentBorder = Ink,
    urgent = AlertDark,
    background = NightBg,
    textPrimary = Sand2,
    textSecondary = SandMuted2,
    textMuted = SandMuted2,
    textFaint = SandMuted2,
    cardSurface = NightCard,
    cardBorder = Sand2,
    cardSurfaceSubtle = NightCard,
    cardBorderSubtle = Sand2,
    trackBackground = NightBg,
    trackActive = Yellow,
    trackDone = Sand2,
    ringTrack = NightRingTrack,
    badgeBackground = NightCard,
    badgeContent = Sand2,
    sizeBadgeBorder = Sand2,
    sizeBadgeContent = Sand2,
    navBackground = NightCard,
    navBorder = Sand2,
    statusChipBackground = Yellow,
    statusChipContent = Ink,
    qrPanelBackground = CardWhite,
    qrPanelBorder = Ink,
    qrInk = QrInk,
    qrLabel = QrLabel,
    outlineButtonBorder = Sand2,
    timelineRail = Sand2,
    timelineDotInactive = NightCard,
    headerIconBackground = NightCard,
    infoAccent = Yellow,
    onInfoAccent = Ink,
    infoSurface = NightCard,
    infoBorder = Sand2,
    hardShadow = Sand2,
    borderStrong = Sand2,
    alertFill = AlertFill,
    alertText = AlertDark,
    monoLabel = SandMuted2,
)

internal val LightPaczkofastColors = PaczkofastColors(
    accent = Yellow,
    onAccent = Ink,
    accentDisabled = YellowDisabledLight,
    onAccentDisabled = OnYellowDisabledLight,
    accentText = Ink,
    accentBorder = Ink,
    urgent = AlertText,
    background = Cream,
    textPrimary = Ink,
    textSecondary = InkMutedNb,
    textMuted = InkMutedNb,
    textFaint = InkMutedNb,
    cardSurface = CardWhite,
    cardBorder = Ink,
    cardSurfaceSubtle = Cream,
    cardBorderSubtle = Ink,
    trackBackground = Cream,
    trackActive = Yellow,
    trackDone = Cream,
    ringTrack = CardWhite,
    badgeBackground = CardWhite,
    badgeContent = Ink,
    sizeBadgeBorder = Ink,
    sizeBadgeContent = Ink,
    navBackground = CardWhite,
    navBorder = Ink,
    statusChipBackground = Yellow,
    statusChipContent = Ink,
    qrPanelBackground = CardWhite,
    qrPanelBorder = Ink,
    qrInk = QrInk,
    qrLabel = QrLabel,
    outlineButtonBorder = Ink,
    timelineRail = Ink,
    timelineDotInactive = CardWhite,
    headerIconBackground = CardWhite,
    infoAccent = Yellow,
    onInfoAccent = Ink,
    infoSurface = CardWhite,
    infoBorder = Ink,
    hardShadow = Ink,
    borderStrong = Ink,
    alertFill = AlertFill,
    alertText = AlertText,
    monoLabel = InkMutedNb,
)

/**
 * CompositionLocal carrying the app-specific color roles.
 * Provided by [PaczkofastTheme]; read via `PaczkofastTheme.colors`.
 */
val LocalPaczkofastColors = staticCompositionLocalOf { DarkPaczkofastColors }
