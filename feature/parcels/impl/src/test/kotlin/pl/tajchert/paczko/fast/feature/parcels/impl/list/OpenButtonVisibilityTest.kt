package pl.tajchert.paczko.fast.feature.parcels.impl.list

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode

class OpenButtonVisibilityTest {

    @Test
    fun firstShowsOnlyFirstCollectableRow() {
        val first = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.FIRST,
            canCollect = true,
            firstButtonAlreadyUsed = false,
        )
        val second = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.FIRST,
            canCollect = true,
            firstButtonAlreadyUsed = first.usedFirstButton,
        )

        assertEquals(true, first.show)
        assertEquals(true, first.usedFirstButton)
        assertEquals(false, second.show)
        assertEquals(true, second.usedFirstButton)
    }

    @Test
    fun firstSkipsRowsThatCannotCollect() {
        val skipped = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.FIRST,
            canCollect = false,
            firstButtonAlreadyUsed = false,
        )
        val firstCollectable = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.FIRST,
            canCollect = true,
            firstButtonAlreadyUsed = skipped.usedFirstButton,
        )

        assertEquals(false, skipped.show)
        assertEquals(false, skipped.usedFirstButton)
        assertEquals(true, firstCollectable.show)
        assertEquals(true, firstCollectable.usedFirstButton)
    }

    @Test
    fun allShowsEveryCollectableRowWithoutConsumingFirstSlot() {
        val decision = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.ALL,
            canCollect = true,
            firstButtonAlreadyUsed = false,
        )

        assertEquals(true, decision.show)
        assertEquals(false, decision.usedFirstButton)
    }

    @Test
    fun noneShowsNoRows() {
        val decision = decideOpenButtonVisibility(
            mode = ParcelListOpenButtonMode.NONE,
            canCollect = true,
            firstButtonAlreadyUsed = false,
        )

        assertEquals(false, decision.show)
        assertEquals(false, decision.usedFirstButton)
    }
}
