package pl.tajchert.paczko.fast.feature.parcels.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent

class TrackingEventLabelTest {

    @Test
    fun curatedCodesMapToEnglishLabels() {
        assertEquals("Out for delivery", trackingEventLabel("OUT_FOR_DELIVERY"))
        assertEquals("Delivered", trackingEventLabel("DELIVERED"))
        assertEquals("Picked up", trackingEventLabel("CLAIMED"))
        assertEquals("Ready for pickup", trackingEventLabel("READY_TO_PICKUP"))
    }

    @Test
    fun unmappedCodeFallsBackToHumanizedStatus() {
        assertEquals("Some future status", trackingEventLabel("SOME_FUTURE_STATUS"))
    }

    @Test
    fun timelineEventsMarkNewestAsCurrentAndNoneUpcoming() {
        val events = listOf(
            TrackingEvent("DELIVERED", "2026-05-26T13:00:13.328Z"),
            TrackingEvent("CONFIRMED", "2026-05-25T14:41:46.362Z"),
        )

        val timeline = trackingTimelineEvents(events)

        assertEquals(2, timeline.size)
        assertEquals("Delivered", timeline[0].label)
        assertTrue(timeline[0].isCurrent)
        assertFalse(timeline[1].isCurrent)
        assertTrue(timeline.none { it.isUpcoming })
    }

    @Test
    fun timelineEventLabelAndTimeArePopulated() {
        val timeline = trackingTimelineEvents(
            listOf(TrackingEvent("OUT_FOR_DELIVERY", "2026-05-26T05:18:54.780Z")),
        )
        assertEquals("Out for delivery", timeline[0].label)
        assertTrue(timeline[0].time != null && timeline[0].time!!.isNotBlank())
    }
}
