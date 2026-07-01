package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import pl.tajchert.paczko.fast.core.model.parcel.Parcel

data class ParcelDetailUiState(
    val parcel: Parcel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
