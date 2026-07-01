package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
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
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.parcel)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}

private class FakeParcelRepository(
    parcels: List<Parcel>,
    private val emitParcel: Boolean = true,
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
    mobileCollectPossible = true,
)
