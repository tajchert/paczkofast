package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.CollectApiException
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class DemoCollectRepositoryTest {

    private val geo = GeoPoint(52.2297, 21.0122, 5.0)

    @Test
    fun `success scenario walks the whole sequence without throwing`() = runTest {
        val r = DemoCollectRepository()
        val session = r.validate(DemoData.READY_SUCCESS, "000000", geo)
        r.open(session)
        r.pollStatus(session, ExpectedCompartmentStatus.OPENED)
        r.pollStatus(session, ExpectedCompartmentStatus.CLOSED)
        r.closed(session)
        r.claim(session, listOf(DemoData.READY_SUCCESS))
    }

    @Test
    fun `session-expired parcel throws sessionExpired at open`() = runTest {
        val r = DemoCollectRepository()
        val session = r.validate(DemoData.READY_SESSION_EXPIRED, "000000", geo)
        val ex = assertFailsWith<CollectApiException> { r.open(session) }
        assertEquals("sessionExpired", ex.apiValue)
    }

    @Test
    fun `box-offline parcel throws boxMachineNotFound at validate`() = runTest {
        val r = DemoCollectRepository()
        val ex = assertFailsWith<CollectApiException> { r.validate(DemoData.READY_BOX_OFFLINE, "000000", geo) }
        assertEquals("boxMachineNotFound", ex.apiValue)
    }

    @Test
    fun `post-open-fail parcel opens then throws at closed`() = runTest {
        val r = DemoCollectRepository()
        val session = r.validate(DemoData.READY_POST_OPEN_FAIL, "000000", geo)
        r.open(session)
        r.pollStatus(session, ExpectedCompartmentStatus.OPENED)
        r.pollStatus(session, ExpectedCompartmentStatus.CLOSED)
        val ex = assertFailsWith<CollectApiException> { r.closed(session) }
        assertEquals("unknown", ex.apiValue)
    }

    @Test
    fun `slow-close parcel still completes`() = runTest {
        val r = DemoCollectRepository()
        val session = r.validate(DemoData.READY_SLOW_CLOSE, "000000", geo)
        r.open(session)
        r.pollStatus(session, ExpectedCompartmentStatus.OPENED)
        r.pollStatus(session, ExpectedCompartmentStatus.CLOSED) // long delay, skipped by virtual time
        r.closed(session)
        r.claim(session, listOf(DemoData.READY_SLOW_CLOSE))
    }
}
