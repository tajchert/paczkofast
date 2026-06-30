package pl.tajchert.paczko.fast.core.model.collect

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectModelsTest {
    @Test
    fun sessionExpiredAndInvalidSessionStateCanRestartValidation() {
        assertTrue(CollectErrorCode.SessionExpired.canRestartValidation)
        assertTrue(CollectErrorCode.InvalidSessionState.canRestartValidation)
        assertFalse(CollectErrorCode.InvalidParcelState.canRestartValidation)
    }
}
