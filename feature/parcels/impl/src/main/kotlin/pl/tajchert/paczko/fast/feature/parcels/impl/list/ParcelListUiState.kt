package pl.tajchert.paczko.fast.feature.parcels.impl.list

import pl.tajchert.paczko.fast.core.model.parcel.Parcel

data class ParcelListUiState(
    val parcels: List<Parcel> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
