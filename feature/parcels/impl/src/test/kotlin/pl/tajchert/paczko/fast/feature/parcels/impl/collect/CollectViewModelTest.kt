package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.CollectParcelUseCase
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.ParcelOperations
import pl.tajchert.paczko.fast.core.model.parcel.PickupPoint
import pl.tajchert.paczko.fast.core.testing.repository.FakeUserPreferencesRepository
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
        val viewModel = CollectViewModel(
            useCase,
            parcelRepository,
            FakeLocationProvider(),
            FakeUserPreferencesRepository(),
        )

        viewModel.start("123")

        assertEquals(CollectState.Completed, viewModel.uiState.value.state)
        assertEquals("456", collectRepository.lastOpenCode)
        assertEquals(listOf("123"), collectRepository.claimedShipmentNumbers)
        assertEquals(1, viewModel.uiState.value.members.size)
    }

    @Test
    fun startMultiCompartmentValidatesTappedParcelAndClaimsAllMembers() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcels = listOf(
                parcel(
                    shipmentNumber = "111",
                    openCode = "aaa",
                    multiCompartmentUuid = "mc-1",
                    multiPackageShipmentNumbers = listOf("111", "222"),
                ),
                parcel(shipmentNumber = "222", openCode = "bbb", multiCompartmentUuid = "mc-1"),
            ),
        )
        val collectRepository = FakeCollectRepository()
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.start("111")

        assertEquals(CollectState.Completed, viewModel.uiState.value.state)
        // Opens with the tapped parcel's own open code…
        assertEquals("aaa", collectRepository.lastOpenCode)
        // …and claims every parcel sharing the compartment.
        assertEquals(listOf("111", "222"), collectRepository.claimedShipmentNumbers)
        assertEquals(2, viewModel.uiState.value.members.size)
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
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Tej paczki nie można otworzyć zdalnie",
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
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Tej paczki nie można otworzyć zdalnie",
                canRetryFromValidation = false,
            ),
            viewModel.uiState.value.state,
        )
        assertEquals(0, collectRepository.validateCount)
    }

    @Test
    fun closeTimeoutAfterOpenSurfacesBoxAlreadyOpenFailure() = runTest {
        // Mirrors the real incident: the box opened, then the CLOSED long-poll
        // timed out. The UI must treat it as collected-but-unconfirmed (snackbar),
        // not a full-screen error.
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val collectRepository = FakeCollectRepository(timeoutOnClosePoll = true)
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = collectRepository,
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.start("123")

        val state = viewModel.uiState.value.state
        assertTrue(state is CollectState.Failed && state.boxAlreadyOpen)
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
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
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
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.onLocationPermissionDenied("123")
        viewModel.start("123")

        assertEquals(
            CollectState.Failed(
                message = "Włącz dostęp do lokalizacji",
                canRetryFromValidation = false,
            ),
            viewModel.uiState.value.state,
        )
        assertEquals(0, collectRepository.validateCount)
    }

    @Test
    fun armExposesLockerNameAndDistanceWhenCoordinatesPresent() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = FakeCollectRepository(),
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
            // ~111 m north of the locker at 50.061 / 19.938
            locationProvider = FakeLocationProvider(
                GeoPoint(latitude = 50.062, longitude = 19.938, accuracy = 5.0),
            ),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.arm("123")

        assertEquals(CollectState.Idle, viewModel.uiState.value.state)
        assertEquals("KRA01A", viewModel.uiState.value.lockerName)
        val distance = viewModel.uiState.value.distanceMeters
        assertTrue("expected ~111 m, was $distance", distance != null && distance in 105..117)
    }

    @Test
    fun armLeavesDistanceNullWhenLockerHasNoCoordinates() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456", latitude = null, longitude = null),
        )
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = FakeCollectRepository(),
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.arm("123")

        assertEquals(CollectState.Idle, viewModel.uiState.value.state)
        assertEquals("KRA01A", viewModel.uiState.value.lockerName)
        assertNull(viewModel.uiState.value.distanceMeters)
    }

    @Test
    fun armDoesNotClobberFlowStartedWhileLocationPending() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        // Gates the location STREAM (not currentLocation()) so arm()'s
        // locationJob genuinely suspends on the live code path — arm() now
        // collects locationProvider.locationUpdates() rather than calling
        // currentLocation(), so gating the latter (as this test used to)
        // no longer suspends anything and made the test vacuous.
        //
        // With the gate on the stream, start()'s locationJob?.cancel() races
        // the pending fix: cancelling a coroutine suspended on
        // CompletableDeferred.await() resolves it with
        // CancellationException immediately, so in this single-threaded
        // test dispatcher the cancellation alone already stops the stale
        // fix from reaching the `it.state !is CollectState.Idle` guard.
        // On a real device the two can race across threads instead, so both
        // defenses matter together; removing BOTH — the cancel() call in
        // start() and the Idle guard in arm()'s locationUpdates().collect —
        // reopens the clobbering bug and fails this test (verified manually
        // by deleting both locally: distanceMeters becomes non-null after
        // completion; restored afterwards, no production change kept).
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        val gatedLocation = object : LocationProvider {
            override suspend fun currentLocation(): GeoPoint =
                GeoPoint(latitude = 50.061, longitude = 19.938, accuracy = 5.0)

            override fun locationUpdates(): Flow<GeoPoint> =
                kotlinx.coroutines.flow.flow {
                    gate.await()
                    emit(GeoPoint(latitude = 50.061, longitude = 19.938, accuracy = 5.0))
                }
        }
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(
                repository = FakeCollectRepository(),
                locationProvider = FakeLocationProvider(),
            ),
            parcelRepository = parcelRepository,
            locationProvider = gatedLocation,
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.arm("123")        // starts locationJob, which suspends on gate.await()
        assertNull(viewModel.uiState.value.distanceMeters)

        viewModel.start("123")      // runs the collect flow to Completed and cancels locationJob
        assertEquals(CollectState.Completed, viewModel.uiState.value.state)

        gate.complete(Unit)         // release the (now-cancelled) location stream
        testScheduler.advanceUntilIdle()

        // The pending location update must NOT clobber the completed flow.
        assertEquals(CollectState.Completed, viewModel.uiState.value.state)
        assertNull(viewModel.uiState.value.distanceMeters)
    }

    @Test
    fun armLoadsOpenModeFromPreferences() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val prefs = FakeUserPreferencesRepository()
        prefs.setLockerOpenMode(LockerOpenMode.NEARBY)
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(FakeCollectRepository(), FakeLocationProvider()),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = prefs,
        )

        viewModel.arm("123")
        advanceUntilIdle()

        assertEquals(LockerOpenMode.NEARBY, viewModel.uiState.value.openMode)
    }

    @Test
    fun armStreamsDistanceAndAccuracyAndFlipsNearbyReady() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(
                shipmentNumber = "123",
                openCode = "456",
                latitude = 52.100000,
                longitude = 21.000000,
            ),
        )
        // First fix is coarse and far; second is close and precise.
        val far = GeoPoint(latitude = 52.20, longitude = 21.00, accuracy = 60.0)
        val near = GeoPoint(latitude = 52.100050, longitude = 21.000000, accuracy = 8.0)
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(FakeCollectRepository(), FakeLocationProvider()),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(location = near, updates = listOf(far, near)),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.arm("123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(8, state.accuracyMeters)
        assertTrue(state.distanceMeters != null && state.distanceMeters!! < 50)
        assertTrue(state.nearbyReady)
    }
}

private class FakeParcelRepository(
    parcel: Parcel?,
) : ParcelRepository {
    constructor(parcels: List<Parcel>) : this(null) {
        parcelState.value = parcels
    }

    private val parcelState = MutableStateFlow(listOfNotNull(parcel))

    override fun observeParcels(): Flow<List<Parcel>> = parcelState

    override fun observeParcel(shipmentNumber: String): Flow<Parcel?> =
        parcelState.map { parcels ->
            parcels.firstOrNull { it.shipmentNumber == shipmentNumber }
        }

    override suspend fun refreshTrackedParcels() = Unit

    override fun observeParcelDetails(shipmentNumber: String): Flow<pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails> =
        MutableStateFlow(pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails())

    override suspend fun refreshParcelDetails(shipmentNumber: String) = Unit
}

private class FakeCollectRepository(
    private val timeoutOnClosePoll: Boolean = false,
) : CollectRepository {
    var lastOpenCode: String? = null
    var validateCount = 0

    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String {
        validateCount += 1
        lastOpenCode = openCode
        return "session"
    }

    override suspend fun open(sessionUuid: String) = Unit

    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) {
        if (timeoutOnClosePoll && expectedStatus == ExpectedCompartmentStatus.CLOSED) {
            throw java.net.SocketTimeoutException("timeout")
        }
    }

    override suspend fun closed(sessionUuid: String) = Unit

    var claimedShipmentNumbers: List<String>? = null

    override suspend fun claim(sessionUuid: String, shipmentNumbers: List<String>) {
        claimedShipmentNumbers = shipmentNumbers
    }
}

private class FakeLocationProvider(
    private val location: GeoPoint = GeoPoint(latitude = 52.1, longitude = 21.0, accuracy = 12.0),
    private val updates: List<GeoPoint> = listOf(location),
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = location
    override fun locationUpdates(): Flow<GeoPoint> = updates.asFlow()
}

private fun parcel(
    shipmentNumber: String,
    openCode: String?,
    latitude: Double? = 50.061,
    longitude: Double? = 19.938,
    multiCompartmentUuid: String? = null,
    multiPackageShipmentNumbers: List<String> = emptyList(),
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
        latitude = latitude,
        longitude = longitude,
    ),
    expiryDate = "2026-07-02T12:00:00Z",
    storedDate = "2026-07-01T12:00:00Z",
    operations = ParcelOperations(collect = true),
    multiCompartmentUuid = multiCompartmentUuid,
    multiPackageShipmentNumbers = multiPackageShipmentNumbers,
)
