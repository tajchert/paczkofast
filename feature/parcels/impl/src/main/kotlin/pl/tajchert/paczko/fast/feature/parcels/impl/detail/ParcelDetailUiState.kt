package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.model.parcel.Parcel
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent

@Immutable
data class ParcelDetailUiState(
    val parcel: Parcel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val events: ImmutableList<TrackingEvent> = persistentListOf(),
    val sizeCode: String? = null,
    val senderName: String? = null,
    val shipmentType: String? = null,
)
