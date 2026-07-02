package pl.tajchert.paczko.fast.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Paczkofast app design system.
 *
 * Two fixed brand palettes from the "Black Amber" design direction:
 * - Dark (default): near-black warm surfaces with amber accent
 * - Light: warm paper surfaces with the same amber accent
 *
 * Raw colors below should NOT be used directly in composables. Use either
 * `MaterialTheme.colorScheme` (for Material components) or the app-specific
 * roles on [PaczkofastTheme.colors] (see [PaczkofastColors]).
 */

// Shared brand accent
internal val Amber = Color(0xFFF2AF1D)
internal val OnAmber = Color(0xFF201500)
internal val AmberDeep = Color(0xFFB97F00)

// Disabled primary button ("Log in" before the code is complete)
internal val AmberDisabledDark = Color(0xFF2A2415)
internal val OnAmberDisabledDark = Color(0xFF8A7A50)
internal val AmberDisabledLight = Color(0xFFEDE2C3)
internal val OnAmberDisabledLight = Color(0xFFA79465)

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
)

internal val DarkPaczkofastColors = PaczkofastColors(
    accent = Amber,
    onAccent = OnAmber,
    accentDisabled = AmberDisabledDark,
    onAccentDisabled = OnAmberDisabledDark,
    accentText = Amber,
    urgent = UrgentDark,
    background = Night0,
    textPrimary = Sand,
    textSecondary = SandSoft,
    textMuted = SandMuted,
    textFaint = SandFaint,
    cardSurface = Night1,
    cardBorder = Color(0xFFF5D78C).copy(alpha = 0.12f),
    cardSurfaceSubtle = Night2,
    cardBorderSubtle = Color(0xFFF5D78C).copy(alpha = 0.07f),
    trackBackground = NightTrack,
    trackActive = Amber,
    trackDone = SandFaint,
    badgeBackground = NightBadge,
    badgeContent = SandSoft,
    sizeBadgeBorder = Color(0xFFF5D78C).copy(alpha = 0.25f),
    sizeBadgeContent = SandSoft,
    navBackground = NightNav,
    navBorder = Color(0xFFF5D78C).copy(alpha = 0.09f),
    statusChipBackground = Amber.copy(alpha = 0.15f),
    statusChipContent = Amber,
    qrPanelBackground = Color.White,
    qrPanelBorder = Color.Transparent,
    qrInk = QrInk,
    qrLabel = QrLabel,
    outlineButtonBorder = Color(0xFFF5D78C).copy(alpha = 0.3f),
    timelineRail = NightRail,
    timelineDotInactive = SandFaint,
    headerIconBackground = Night1,
    infoAccent = InfoBlue,
    onInfoAccent = Night0,
    infoSurface = NightBlue,
    infoBorder = InfoBlue.copy(alpha = 0.32f),
)

internal val LightPaczkofastColors = PaczkofastColors(
    accent = Amber,
    onAccent = OnAmber,
    accentDisabled = AmberDisabledLight,
    onAccentDisabled = OnAmberDisabledLight,
    accentText = AmberDeep,
    urgent = UrgentLight,
    background = Paper0,
    textPrimary = InkStrong,
    textSecondary = InkSoft,
    textMuted = InkMuted,
    textFaint = InkFaint,
    cardSurface = Paper1,
    cardBorder = PaperBorder,
    cardSurfaceSubtle = Paper2,
    cardBorderSubtle = PaperBorderSubtle,
    trackBackground = PaperTrack,
    trackActive = TrackActiveLight,
    trackDone = TrackDoneLight,
    badgeBackground = PaperBadge,
    badgeContent = InkSoft,
    sizeBadgeBorder = PaperBorderBadge,
    sizeBadgeContent = InkBadge,
    navBackground = Paper1,
    navBorder = PaperBorder,
    statusChipBackground = Amber.copy(alpha = 0.18f),
    statusChipContent = AmberDeep,
    qrPanelBackground = Color(0xFFFAF8F2),
    qrPanelBorder = PaperBorder,
    qrInk = QrInk,
    qrLabel = QrLabel,
    outlineButtonBorder = PaperBorderBadge,
    timelineRail = PaperTrack,
    timelineDotInactive = TrackDoneLight,
    headerIconBackground = Paper1,
    infoAccent = InfoBlueDeep,
    onInfoAccent = Color.White,
    infoSurface = PaperBlue,
    infoBorder = InfoBlueDeep.copy(alpha = 0.28f),
)

/**
 * CompositionLocal carrying the app-specific color roles.
 * Provided by [PaczkofastTheme]; read via `PaczkofastTheme.colors`.
 */
val LocalPaczkofastColors = staticCompositionLocalOf { DarkPaczkofastColors }
