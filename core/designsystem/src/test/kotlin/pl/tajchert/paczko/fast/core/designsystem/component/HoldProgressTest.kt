package pl.tajchert.paczko.fast.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoldProgressTest {

    @Test
    fun progressIsZeroBeforePress() {
        val controller = HoldProgress(holdDurationMillis = 1000)
        assertEquals(0f, controller.progressAt(500), 0.0001f)
    }

    @Test
    fun progressIsHalfwayAtHalfDuration() {
        val controller = HoldProgress(holdDurationMillis = 1000)
        controller.onPress(nowMillis = 0)
        assertEquals(0.5f, controller.progressAt(500), 0.0001f)
    }

    @Test
    fun progressClampsToOne() {
        val controller = HoldProgress(holdDurationMillis = 1000)
        controller.onPress(nowMillis = 0)
        assertEquals(1f, controller.progressAt(5000), 0.0001f)
    }

    @Test
    fun completionFiresExactlyOnce() {
        val controller = HoldProgress(holdDurationMillis = 1000)
        controller.onPress(nowMillis = 0)
        assertFalse(controller.consumeCompletion(999))
        assertTrue(controller.consumeCompletion(1000))
        assertFalse(controller.consumeCompletion(1500))
    }

    @Test
    fun releaseResetsProgressAndAllowsRefire() {
        val controller = HoldProgress(holdDurationMillis = 1000)
        controller.onPress(nowMillis = 0)
        assertTrue(controller.consumeCompletion(1000))
        controller.onRelease()
        assertEquals(0f, controller.progressAt(1200), 0.0001f)
        controller.onPress(nowMillis = 2000)
        assertFalse(controller.consumeCompletion(2999))
        assertTrue(controller.consumeCompletion(3000))
    }
}
