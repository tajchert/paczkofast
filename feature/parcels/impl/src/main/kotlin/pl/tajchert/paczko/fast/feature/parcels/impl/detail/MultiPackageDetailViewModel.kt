package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.tajchert.paczko.fast.core.domain.ObserveParcelsUseCase
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.feature.parcels.impl.humanizeStatus
import pl.tajchert.paczko.fast.feature.parcels.impl.lockerLine
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelSizeLabel
import pl.tajchert.paczko.fast.feature.parcels.impl.parcelTitle
import pl.tajchert.paczko.fast.feature.parcels.impl.formatShipmentNumber
import pl.tajchert.paczko.fast.feature.parcels.impl.pickupCountdown

/** One parcel row on the multi-package box detail (7a). */
data class BoxMember(
    val shipmentNumber: String,
    val title: String,
    val shipmentNumberLine: String,
    val sizeLabel: String?,
)

data class MultiPackageDetailUiState(
    val isLoading: Boolean = true,
    val statusLabel: String? = null,
    val members: List<BoxMember> = emptyList(),
    val lockerLine: String? = null,
    val deadlineText: String? = null,
    val countdownText: String? = null,
    val progress: Float? = null,
    val urgent: Boolean = false,
    val qrCode: String? = null,
    val openCode: String? = null,
    val canCollect: Boolean = false,
    /** The member to validate/open the box with. */
    val representativeShipmentNumber: String? = null,
    /**
     * The parcel carrying the shared box identity, exposed so the screen can
     * derive presentation-only details (e.g. delivered/picked-up state) the
     * same way [ParcelDetailScreen] does from its own [Parcel].
     */
    val representative: Parcel? = null,
)

@HiltViewModel(assistedFactory = MultiPackageDetailViewModel.Factory::class)
class MultiPackageDetailViewModel @AssistedInject constructor(
    @Assisted private val shipmentNumber: String,
    observeParcels: ObserveParcelsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shipmentNumber: String): MultiPackageDetailViewModel
    }

    val uiState: StateFlow<MultiPackageDetailUiState> = observeParcels()
        .map { parcels -> buildState(parcels) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MultiPackageDetailUiState(isLoading = true),
        )

    private fun buildState(parcels: List<Parcel>): MultiPackageDetailUiState {
        val target = parcels.firstOrNull { it.shipmentNumber == shipmentNumber }
            ?: return MultiPackageDetailUiState(isLoading = false)
        val uuid = target.multiCompartmentUuid
        val members = if (uuid.isNullOrBlank()) {
            listOf(target)
        } else {
            parcels.filter { it.multiCompartmentUuid == uuid }.ifEmpty { listOf(target) }
        }
        // The parcel carrying the member list is the one used to open the box.
        val representative = members.firstOrNull { it.multiPackageShipmentNumbers.isNotEmpty() } ?: target
        val countdown = pickupCountdown(representative)
        return MultiPackageDetailUiState(
            isLoading = false,
            statusLabel = humanizeStatus(representative.status),
            members = members.map { parcel ->
                BoxMember(
                    shipmentNumber = parcel.shipmentNumber,
                    title = parcelTitle(parcel),
                    shipmentNumberLine = formatShipmentNumber(parcel.shipmentNumber),
                    sizeLabel = parcelSizeLabel(parcel.parcelSize),
                )
            },
            lockerLine = lockerLine(representative),
            deadlineText = countdown?.deadlineText,
            countdownText = countdown?.countdownText,
            progress = countdown?.progress,
            urgent = countdown?.urgent == true,
            qrCode = representative.qrCode,
            openCode = representative.openCode,
            canCollect = representative.canCollectRemotely,
            representativeShipmentNumber = representative.shipmentNumber,
            representative = representative,
        )
    }
}
