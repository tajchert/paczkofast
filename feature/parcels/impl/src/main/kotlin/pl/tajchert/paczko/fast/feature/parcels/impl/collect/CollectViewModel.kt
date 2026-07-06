package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.common.location.isWithinNearbyThreshold
import pl.tajchert.paczko.fast.core.common.location.metersToLocker
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository
import pl.tajchert.paczko.fast.core.domain.CollectParcelUseCase
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelSizeLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelTitle

@HiltViewModel
class CollectViewModel @Inject constructor(
    private val collectParcel: CollectParcelUseCase,
    private val parcelRepository: ParcelRepository,
    private val locationProvider: LocationProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectUiState())
    val uiState: StateFlow<CollectUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var locationJob: Job? = null
    private var startedShipmentNumber: String? = null
    private var armedShipmentNumber: String? = null

    fun arm(shipmentNumber: String) {
        if (armedShipmentNumber == shipmentNumber) return
        if (startedShipmentNumber == shipmentNumber) return
        armedShipmentNumber = shipmentNumber
        viewModelScope.launch {
            val mode = userPreferencesRepository.userPreferences.first().lockerOpenMode
            val members = compartmentMembers(shipmentNumber)
            val pickup = members.firstOrNull { it.shipmentNumber == shipmentNumber }?.pickupPoint
                ?: members.firstOrNull()?.pickupPoint
            _uiState.update {
                if (it.state !is CollectState.Idle) {
                    it
                } else {
                    it.copy(
                        openMode = mode,
                        lockerName = pickup?.name,
                        members = members.map(::toCollectMember).toImmutableList(),
                    )
                }
            }
            locationJob?.cancel()
            // Intentionally long-lived: this keeps collecting location fixes
            // (to refresh distance/accuracy) until start()/onCleared() cancels
            // it. Do not turn this into a single-shot collect.
            locationJob = launch {
                locationProvider.locationUpdates().collect { fix ->
                    val distance = metersToLocker(
                        from = fix,
                        lockerLatitude = pickup?.latitude,
                        lockerLongitude = pickup?.longitude,
                    )
                    _uiState.update {
                        if (it.state !is CollectState.Idle) {
                            it
                        } else {
                            it.copy(
                                distanceMeters = distance,
                                accuracyMeters = fix.accuracy.roundToInt(),
                            )
                        }
                    }
                }
            }
        }
    }

    fun start(shipmentNumber: String) {
        if (startedShipmentNumber == shipmentNumber) return
        if (collectJob?.isActive == true) return

        startedShipmentNumber = shipmentNumber
        locationJob?.cancel()
        collectJob = viewModelScope.launch {
            val members = compartmentMembers(shipmentNumber)
            val parcel = members.firstOrNull { it.shipmentNumber == shipmentNumber }
            val openCode = parcel?.openCode
            val memberUi = members.map(::toCollectMember).toImmutableList()
            if (openCode.isNullOrBlank()) {
                _uiState.update {
                    CollectUiState(
                        state = CollectState.Failed(
                            message = "Tej paczki nie można otworzyć zdalnie",
                            canRetryFromValidation = false,
                        ),
                        members = memberUi,
                    )
                }
                return@launch
            }

            val claimNumbers = members.map { it.shipmentNumber }.ifEmpty { listOf(shipmentNumber) }
            collectParcel.collect(shipmentNumber, openCode, claimNumbers).collect { state ->
                _uiState.update { it.copy(state = state, members = memberUi) }
            }
        }
    }

    /**
     * All parcels sharing the tapped parcel's locker compartment (itself plus
     * any siblings with the same [Parcel.multiCompartmentUuid]); just the tapped
     * parcel for a standalone pickup.
     */
    private suspend fun compartmentMembers(shipmentNumber: String): List<Parcel> {
        val all = parcelRepository.observeParcels().first()
        val target = all.firstOrNull { it.shipmentNumber == shipmentNumber } ?: return emptyList()
        val uuid = target.multiCompartmentUuid
        if (uuid.isNullOrBlank()) return listOf(target)
        val siblings = all.filter { it.multiCompartmentUuid == uuid }
        return if (siblings.size >= 2) siblings else listOf(target)
    }

    private fun toCollectMember(parcel: Parcel) = CollectMember(
        shipmentNumber = parcel.shipmentNumber,
        title = parcelTitle(parcel),
        sizeLabel = parcelSizeLabel(parcel.parcelSize),
    )

    fun onLocationPermissionDenied(shipmentNumber: String) {
        startedShipmentNumber = shipmentNumber
        _uiState.update {
            CollectUiState(
                state = CollectState.Failed(
                    message = "Włącz dostęp do lokalizacji",
                    canRetryFromValidation = false,
                ),
            )
        }
    }

    override fun onCleared() {
        locationJob?.cancel()
    }
}

@Immutable
data class CollectUiState(
    val state: CollectState = CollectState.Idle,
    val lockerName: String? = null,
    val distanceMeters: Int? = null,
    val accuracyMeters: Int? = null,
    val openMode: LockerOpenMode = LockerOpenMode.HOLD,
    val members: ImmutableList<CollectMember> = persistentListOf(),
) {
    val nearbyReady: Boolean get() = isWithinNearbyThreshold(distanceMeters, accuracyMeters)
}

/** One parcel in the compartment being collected — drives the checklist/summary. */
@Immutable
data class CollectMember(
    val shipmentNumber: String,
    val title: String,
    val sizeLabel: String? = null,
)
