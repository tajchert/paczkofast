package pl.tajchert.paczko.fast.core.model

/**
 * Priority levels for tasks.
 *
 * ## Usage
 *
 * Priority is used for:
 * 1. Visual differentiation in the UI (colors, icons)
 * 2. Sorting tasks (HIGH first, LOW last)
 * 3. Filtering (show only high priority tasks)
 *
 * ## Ordinal-based Comparison
 *
 * The enum is ordered from LOW to HIGH intentionally.
 * This allows natural sorting using `sortedByDescending { it.priority.ordinal }`
 * to show high priority tasks first.
 */
enum class TaskPriority {
    /**
     * Low priority - can be done whenever time permits.
     */
    LOW,

    /**
     * Medium priority - should be done soon but not urgent.
     */
    MEDIUM,

    /**
     * High priority - should be done as soon as possible.
     */
    HIGH,
}
