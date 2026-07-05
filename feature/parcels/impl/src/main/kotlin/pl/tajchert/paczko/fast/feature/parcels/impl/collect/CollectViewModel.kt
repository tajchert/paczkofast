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
import pl.tajchert.paczko.fast.core.common.location.LocationProvider
import pl.tajchert.paczko.fast.core.common.location.metersToLocker
import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.domain.CollectParcelUseCase
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelSizeLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelTitle

@HiltViewModel
class CollectViewModel @Inject constructor(
    private val collectParcel: CollectParcelUseCase,
    private val parcelRepository: ParcelRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CollectUiState())
    val uiState: StateFlow<CollectUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var startedShipmentNumber: String? = null
    private var armedShipmentNumber: String? = null

    fun arm(shipmentNumber: String) {
        if (armedShipmentNumber == shipmentNumber) return
        if (startedShipmentNumber == shipmentNumber) return
        armedShipmentNumber = shipmentNumber
        viewModelScope.launch {
            val members = compartmentMembers(shipmentNumber)
            val pickup = members.firstOrNull { it.shipmentNumber == shipmentNumber }?.pickupPoint
                ?: members.firstOrNull()?.pickupPoint
            val distance = runCatching {
                metersToLocker(
                    from = locationProvider.currentLocation(),
                    lockerLatitude = pickup?.latitude,
                    lockerLongitude = pickup?.longitude,
                )
            }.getOrNull()
            _uiState.update {
                if (it.state !is CollectState.Idle) {
                    it
                } else {
                    it.copy(
                        lockerName = pickup?.name,
                        distanceMeters = distance,
                        members = members.map(::toCollectMember).toImmutableList(),
                    )
                }
            }
        }
    }

    fun start(shipmentNumber: String) {
        if (startedShipmentNumber == shipmentNumber) return
        if (collectJob?.isActive == true) return

        startedShipmentNumber = shipmentNumber
        collectJob = viewModelScope.launch {
            val members = compartmentMembers(shipmentNumber)
            val parcel = members.firstOrNull { it.shipmentNumber == shipmentNumber }
            val openCode = parcel?.openCode
            val memberUi = members.map(::toCollectMember).toImmutableList()
            if (openCode.isNullOrBlank()) {
                _uiState.update {
                    CollectUiState(
                        state = CollectState.Failed(
                            message = "Parcel cannot be opened remotely",
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
                    message = "Location permission is required",
                    canRetryFromValidation = false,
                ),
            )
        }
    }
}

@Immutable
data class CollectUiState(
    val state: CollectState = CollectState.Idle,
    val lockerName: String? = null,
    val distanceMeters: Int? = null,
    val members: ImmutableList<CollectMember> = persistentListOf(),
)

/** One parcel in the compartment being collected — drives the checklist/summary. */
@Immutable
data class CollectMember(
    val shipmentNumber: String,
    val title: String,
    val sizeLabel: String? = null,
)
