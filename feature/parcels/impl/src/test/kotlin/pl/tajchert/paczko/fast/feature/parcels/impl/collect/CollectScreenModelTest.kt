package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState

class CollectScreenModelTest {

    private fun ui(
        state: CollectState = CollectState.Idle,
        mode: LockerOpenMode = LockerOpenMode.HOLD,
        distance: Int? = null,
        accuracy: Int? = null,
    ) = CollectUiState(
        state = state,
        openMode = mode,
        distanceMeters = distance,
        accuracyMeters = accuracy,
        lockerName = "WAW01A",
    )

    @Test
    fun holdIdleUsesHoldAction() {
        val m = collectScreenModel(CollectState.Idle, ui(mode = LockerOpenMode.HOLD))
        assertEquals(CollectHero.Distance, m.hero)
        assertEquals(CollectAction.HoldOnly, m.action)
        assertFalse(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleReadyEnablesOpenAndShowsOverride() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = 10, accuracy = 8),
        )
        assertEquals(CollectAction.NearbyOpen, m.action)
        assertTrue(m.openEnabled)
        assertTrue(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleFarDisablesOpenWithReason() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = 62, accuracy = 8),
        )
        assertFalse(m.openEnabled)
        assertEquals("Move closer — 62 m away", m.subline)
        assertTrue(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleNoFixWaitsForGps() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = null, accuracy = null),
        )
        assertFalse(m.openEnabled)
        assertEquals("Waiting for a precise GPS fix…", m.subline)
    }

    @Test
    fun openedStateShowsOpenBoxHero() {
        val m = collectScreenModel(CollectState.Opened("s"), ui(state = CollectState.Opened("s")))
        assertEquals(CollectHero.OpenBox, m.hero)
        assertEquals(CollectAction.None, m.action)
    }

    @Test
    fun completedShowsCheckAndBack() {
        val m = collectScreenModel(CollectState.Completed, ui(state = CollectState.Completed))
        assertEquals(CollectHero.Check, m.hero)
        assertEquals(CollectAction.BackOnly, m.action)
    }

    @Test
    fun failedBeforeOpenShowsErrorAndRetry() {
        val failed = CollectState.Failed("boxMachineNotFound", canRetryFromValidation = false)
        val m = collectScreenModel(failed, ui(state = failed))
        assertEquals(CollectHero.Error, m.hero)
        assertEquals(CollectAction.RetrySupport, m.action)
    }
}
