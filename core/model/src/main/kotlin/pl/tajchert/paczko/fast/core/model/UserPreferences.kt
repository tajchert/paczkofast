package pl.tajchert.paczko.fast.core.model

/**
 * User preferences stored locally on the device.
 *
 * ## Storage
 *
 * These preferences are persisted using Proto DataStore (not SharedPreferences).
 * Proto DataStore provides:
 * - Type safety through protocol buffers
 * - Async API that doesn't block the main thread
 * - Data consistency guarantees
 *
 * ## Why Not in DataStore Module?
 *
 * This domain model is separate from the Proto-generated classes in core:datastore.
 * The DataStore module handles storage concerns, while this represents
 * what the rest of the app works with.
 *
 * @property themeBrand The visual theme variant to use
 * @property darkThemeConfig How dark theme should be determined
 * @property sortOrder How tasks should be sorted in the list
 */
data class UserPreferences(
    val themeBrand: ThemeBrand = ThemeBrand.DEFAULT,
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val sortOrder: TaskSortOrder = TaskSortOrder.CREATED_DATE,
)

/**
 * Theme brand options.
 */
enum class ThemeBrand {
    /**
     * Use the app's default color scheme.
     */
    DEFAULT,

    /**
     * Use Android 12+ dynamic colors derived from the user's wallpaper.
     * Falls back to DEFAULT on older Android versions.
     */
    DYNAMIC,
}

/**
 * Dark theme configuration options.
 */
enum class DarkThemeConfig {
    /**
     * Follow the system-wide dark mode setting.
     */
    FOLLOW_SYSTEM,

    /**
     * Always use light theme regardless of system setting.
     */
    LIGHT,

    /**
     * Always use dark theme regardless of system setting.
     */
    DARK,
}

/**
 * Task list sorting options.
 */
enum class TaskSortOrder {
    /**
     * Sort by creation date (newest first).
     */
    CREATED_DATE,

    /**
     * Sort by priority (highest first).
     */
    PRIORITY,

    /**
     * Sort alphabetically by title (A-Z).
     */
    ALPHABETICAL,
}
