package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.CollectParcelUseCase
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule

class CollectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startCollectEmitsCompletedAfterUseCaseCompletes() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val collectRepository = FakeCollectRepository()
        val useCase = CollectParcelUseCase(
            repository = collectRepository,
            locationProvider = FakeLocationProvider(),
        )
        val viewModel = CollectViewModel(useCase, parcelRepository)

        viewModel.start("123")

        assertEquals(CollectState.Completed, viewModel.uiState.value.state)
        assertEquals("456", collectRepository.lastOpenCode)
    }

    @Test
    fun startWithMissingOpenCodeEmitsNonRetryableFailure() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = null),
        )
        val collectRepository = FakeCollectRepository()
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
        )

        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Parcel cannot be opened remotely",
                canRetryFromValidation = false,
            ),
            viewModel.uiState.value.state,
        )
        assertEquals(0, collectRepository.validateCount)
    }

    @Test
    fun startWithBlankOpenCodeEmitsNonRetryableFailure() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "  "),
        )
        val collectRepository = FakeCollectRepository()
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
        )

        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Parcel cannot be opened remotely",
                canRetryFromValidation = false,
            ),
            viewModel.uiState.value.state,
        )
        assertEquals(0, collectRepository.validateCount)
    }

    @Test
    fun startAfterCompletionDoesNotCollectAgain() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val collectRepository = FakeCollectRepository()
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
        )

        viewModel.start("123")
        viewModel.start("123")

        assertEquals(CollectState.Completed, viewModel.uiState.value.state)
        assertEquals(1, collectRepository.validateCount)
    }

    @Test
    fun locationPermissionDeniedEmitsNonRetryableFailureWithoutCollecting() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val collectRepository = FakeCollectRepository()
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
        )

        viewModel.onLocationPermissionDenied("123")
        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Location permission is required",
                canRetryFromValidation = false,
            ),
            viewModel.uiState.value.state,
        )
        assertEquals(0, collectRepository.validateCount)
    }
}

private class FakeParcelRepository(
    parcel: Parcel?,
) : ParcelRepository {
    private val parcelState = MutableStateFlow(listOfNotNull(parcel))

    override fun observeParcels(): Flow<List<Parcel>> = parcelState

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> =
        parcelState.map { parcels ->
            parcels.firstOrNull { it.shipmentNumber == shipmentNumber }
        }

    override suspend fun refreshTrackedParcels() = Unit
}

private class FakeCollectRepository : CollectRepository {
    var lastOpenCode: String? = null
    var validateCount = 0

    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String {
        validateCount += 1
        lastOpenCode = openCode
        return "session"
    }

    override suspend fun open(sessionUuid: String) = Unit

    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) = Unit

    override suspend fun closed(sessionUuid: String) = Unit

    override suspend fun claim(sessionUuid: String, shipmentNumber: String) = Unit
}

private class FakeLocationProvider : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = GeoPoint(
        latitude = 52.1,
        longitude = 21.0,
        accuracy = 12.0,
    )
}

private fun parcel(
    shipmentNumber: String,
    openCode: String?,
) = Parcel(
    shipmentNumber = shipmentNumber,
    status = "ready_to_pickup",
    statusGroup = "ready_to_pickup",
    openCode = openCode,
    qrCode = "QR-$shipmentNumber",
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
