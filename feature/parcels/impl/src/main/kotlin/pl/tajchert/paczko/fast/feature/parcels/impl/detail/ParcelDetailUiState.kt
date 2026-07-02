package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent

data class ParcelDetailUiState(
    val parcel: Parcel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val events: List<TrackingEvent> = emptyList(),
    val sizeCode: String? = null,
    val senderName: String? = null,
    val shipmentType: String? = null,
)
