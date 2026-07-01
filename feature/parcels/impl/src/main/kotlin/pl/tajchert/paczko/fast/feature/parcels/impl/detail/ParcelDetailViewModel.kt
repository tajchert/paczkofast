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
import pl.tajchert.paczko.fast.core.common.result.Result
import pl.tajchert.paczko.fast.core.common.result.asResult
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase

@HiltViewModel(assistedFactory = ParcelDetailViewModel.Factory::class)
class ParcelDetailViewModel @AssistedInject constructor(
    @Assisted private val shipmentNumber: String,
    observeParcel: ObserveParcelUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shipmentNumber: String): ParcelDetailViewModel
    }

    val uiState: StateFlow<ParcelDetailUiState> = observeParcel(shipmentNumber)
        .asResult()
        .map { result ->
            when (result) {
                is Result.Loading -> ParcelDetailUiState(isLoading = true)
                is Result.Success -> ParcelDetailUiState(
                    parcel = result.data,
                    isLoading = false,
                )
                is Result.Error -> ParcelDetailUiState(
                    isLoading = false,
                    errorMessage = result.exception.message ?: "Unable to load parcel",
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ParcelDetailUiState(isLoading = true),
        )
}
