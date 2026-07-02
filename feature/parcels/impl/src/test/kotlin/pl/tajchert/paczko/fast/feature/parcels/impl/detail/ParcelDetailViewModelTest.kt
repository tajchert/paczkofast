package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.GetParcelDetailsUseCase
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule

class ParcelDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun exposesParcelForRequestedShipmentNumber() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123"), parcel("456")),
        )
        val viewModel = ParcelDetailViewModel(
            shipmentNumber = "456",
            observeParcel = ObserveParcelUseCase(repository),
            getParcelDetails = GetParcelDetailsUseCase(repository),
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("456", viewModel.uiState.value.parcel?.shipmentNumber)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun exposesNullForMissingShipmentNumber() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
        )
        val viewModel = ParcelDetailViewModel(
            shipmentNumber = "missing",
            observeParcel = ObserveParcelUseCase(repository),
            getParcelDetails = GetParcelDetailsUseCase(repository),
        )

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.parcel)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun exposesLoadingBeforeParcelEmits() = runTest {
        val repository = FakeParcelRepository(
            parcels = emptyList(),
            emitParcel = false,
        )
        val viewModel = ParcelDetailViewModel(
            shipmentNumber = "123",
            observeParcel = ObserveParcelUseCase(repository),
            getParcelDetails = GetParcelDetailsUseCase(repository),
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.parcel)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun exposesFetchedDetailFields() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
            details = ParcelDetails(
                events = listOf(TrackingEvent("DELIVERED", "2026-05-26T13:00:13.328Z")),
                sizeCode = "C",
                senderName = "Amazon Polska",
                shipmentType = "courier",
            ),
        )
        val viewModel = ParcelDetailViewModel(
            shipmentNumber = "123",
            observeParcel = ObserveParcelUseCase(repository),
            getParcelDetails = GetParcelDetailsUseCase(repository),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("DELIVERED"), state.events.map { it.status })
        assertEquals("C", state.sizeCode)
        assertEquals("Amazon Polska", state.senderName)
        assertEquals("courier", state.shipmentType)
    }

    @Test
    fun detailsFetchFailureLeavesFieldsEmptyAndParcelLoaded() = runTest {
        val repository = FakeParcelRepository(
            parcels = listOf(parcel("123")),
            failDetails = true,
        )
        val viewModel = ParcelDetailViewModel(
            shipmentNumber = "123",
            observeParcel = ObserveParcelUseCase(repository),
            getParcelDetails = GetParcelDetailsUseCase(repository),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.events.isEmpty())
        assertNull(state.sizeCode)
        assertEquals("123", state.parcel?.shipmentNumber)
    }
}

private class FakeParcelRepository(
    parcels: List<Parcel>,
    private val emitParcel: Boolean = true,
    private val details: ParcelDetails = ParcelDetails(),
    private val failDetails: Boolean = false,
) : ParcelRepository {
    private val parcelState = MutableStateFlow(parcels)

    override fun observeParcels(): Flow<List<Parcel>> = parcelState

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> {
        if (!emitParcel) {
            return emptyFlow()
        }
        return parcelState.map { parcels ->
            parcels.firstOrNull { it.shipmentNumber == shipmentNumber }
        }
    }

    override suspend fun refreshTrackedParcels() = Unit

    override suspend fun getParcelDetails(shipmentNumber: String): ParcelDetails {
        if (failDetails) error("boom")
        return details
    }
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
