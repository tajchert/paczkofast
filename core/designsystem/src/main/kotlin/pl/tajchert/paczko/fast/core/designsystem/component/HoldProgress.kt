package pl.tajchert.paczko.fast.core.designsystem.component

/**
 * Pure, time-driven model of a press-and-hold gesture. The composable feeds it
 * monotonic frame timestamps; it reports fill progress and fires completion
 * exactly once per hold.
 */
class HoldProgress(private val holdDurationMillis: Long) {

    private var startMillis: Long? = null
    private var completionConsumed = false

    fun onPress(nowMillis: Long) {
        startMillis = nowMillis
        completionConsumed = false
    }

    fun onRelease() {
        startMillis = null
        completionConsumed = false
    }

    fun progressAt(nowMillis: Long): Float {
        val start = startMillis ?: return 0f
        if (holdDurationMillis <= 0L) return 1f
        return ((nowMillis - start).toFloat() / holdDurationMillis).coerceIn(0f, 1f)
    }

    /** True exactly once, the first time the hold reaches its full duration. */
    fun consumeCompletion(nowMillis: Long): Boolean {
        val start = startMillis ?: return false
        if (!completionConsumed && nowMillis - start >= holdDurationMillis) {
            completionConsumed = true
            return true
        }
        return false
    }
}
