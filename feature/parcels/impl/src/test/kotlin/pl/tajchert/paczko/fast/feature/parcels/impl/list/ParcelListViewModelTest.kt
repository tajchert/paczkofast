package pl.tajchert.paczko.fast.feature.parcels.impl.list

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.ObserveParcelsUseCase
import pl.tajchert.paczko.fast.core.domain.RefreshParcelsUseCase
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule

class ParcelListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refreshKeepsCachedParcelsVisibleAndClearsRefreshingWhenDone() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
        )
        val viewModel = ParcelListViewModel(
            observeParcels = ObserveParcelsUseCase(repository),
            refreshParcels = RefreshParcelsUseCase(repository),
        )

        viewModel.refresh()

        assertEquals(false, viewModel.uiState.value.isRefreshing)
        assertEquals(listOf("123"), viewModel.uiState.value.parcels.map { it.shipmentNumber })
        assertEquals(1, repository.refreshCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun refreshKeepsCachedParcelsVisibleWhileRefreshIsRunning() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
            suspendRefresh = true,
        )
        val viewModel = ParcelListViewModel(
            observeParcels = ObserveParcelsUseCase(repository),
            refreshParcels = RefreshParcelsUseCase(repository),
        )

        viewModel.refresh()
        repository.awaitRefreshStarted()

        assertEquals(true, viewModel.uiState.value.isRefreshing)
        assertEquals(listOf("123"), viewModel.uiState.value.parcels.map { it.shipmentNumber })

        repository.finishRefresh()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun refreshFailureKeepsCachedParcelsVisibleAndSetsError() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
            refreshError = IllegalStateException("Network unavailable"),
        )
        val viewModel = ParcelListViewModel(
            observeParcels = ObserveParcelsUseCase(repository),
            refreshParcels = RefreshParcelsUseCase(repository),
        )

        viewModel.refresh()

        assertEquals(false, viewModel.uiState.value.isRefreshing)
        assertEquals(listOf("123"), viewModel.uiState.value.parcels.map { it.shipmentNumber })
        assertEquals("Network unavailable", viewModel.uiState.value.errorMessage)
    }
}

private class FakeParcelRepository(
    parcels: List<Parcel>,
    private val suspendRefresh: Boolean = false,
    private val refreshError: Throwable? = null,
) : ParcelRepository {
    private val parcelState = MutableStateFlow(parcels)
    private val refreshStarted = CompletableDeferred<Unit>()
    private val refreshFinished = CompletableDeferred<Unit>()
    var refreshCount = 0
        private set

    override fun observeParcels(): Flow<List<Parcel>> = parcelState

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> = MutableStateFlow(
        parcelState.value.firstOrNull { it.shipmentNumber == shipmentNumber },
    )

    override suspend fun refreshTrackedParcels() {
        refreshCount++
        refreshStarted.complete(Unit)
        if (suspendRefresh) {
            refreshFinished.await()
        }
        refreshError?.let { throw it }
    }

    suspend fun awaitRefreshStarted() {
        refreshStarted.await()
    }

    fun finishRefresh() {
        refreshFinished.complete(Unit)
    }

    override suspend fun getTrackingEvents(shipmentNumber: String): List<TrackingEvent> = emptyList()
}

private fun parcel(number: String) = Parcel(
    shipmentNumber = number,
    status = "ready_to_pickup",
    statusGroup = "ready_to_pickup",
    openCode = "123456",
    qrCode = "QR-$number",
    pickupPoint = PickupPoint(
        name = "KRA01A",
        locationDescription = "Near the main entrance",
        addressLine = "Main Street 1",
        latitude = 50.061,
        longitude = 19.938,
    ),
    expiryDate = "2026-07-02T12:00:00Z",
    storedDate = "2026-07-01T12:00:00Z",
    operations = ParcelOperations(collect = true),
)
