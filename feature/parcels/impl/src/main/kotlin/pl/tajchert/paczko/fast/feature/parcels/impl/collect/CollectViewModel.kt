package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
            val pickup = parcelRepository.observeParcel(shipmentNumber).first()?.pickupPoint
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
            val parcel = parcelRepository.observeParcel(shipmentNumber).first()
            val openCode = parcel?.openCode
            if (openCode.isNullOrBlank()) {
                _uiState.update {
                    CollectUiState(
                        state = CollectState.Failed(
                            message = "Parcel cannot be opened remotely",
                            canRetryFromValidation = false,
                        ),
                    )
                }
                return@launch
            }

            collectParcel.collect(shipmentNumber, openCode).collect { state ->
                _uiState.update { CollectUiState(state = state) }
            }
        }
    }

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

data class CollectUiState(
    val state: CollectState = CollectState.Idle,
    val lockerName: String? = null,
    val distanceMeters: Int? = null,
)
