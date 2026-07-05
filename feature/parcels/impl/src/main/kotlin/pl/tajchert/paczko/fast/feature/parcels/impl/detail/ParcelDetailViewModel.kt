package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.common.result.Result
import pl.tajchert.paczko.fast.core.common.result.asResult
import pl.tajchert.paczko.fast.core.domain.ObserveParcelDetailsUseCase
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase
import pl.tajchert.paczko.fast.core.domain.RefreshParcelDetailsUseCase

@HiltViewModel(assistedFactory = ParcelDetailViewModel.Factory::class)
class ParcelDetailViewModel @AssistedInject constructor(
    @Assisted private val shipmentNumber: String,
    observeParcel: ObserveParcelUseCase,
    observeParcelDetails: ObserveParcelDetailsUseCase,
    refreshParcelDetails: RefreshParcelDetailsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shipmentNumber: String): ParcelDetailViewModel
    }

    init {
        // Refresh the detail cache in the background; the UI reads from Room so
        // the last-known timeline stays visible even when this fails offline.
        viewModelScope.launch {
            runCatching { refreshParcelDetails(shipmentNumber) }
        }
    }

    val uiState: StateFlow<ParcelDetailUiState> = combine(
        observeParcel(shipmentNumber).asResult(),
        observeParcelDetails(shipmentNumber),
    ) { result, detail ->
        val base = ParcelDetailUiState(
            events = detail.events.toImmutableList(),
            sizeCode = detail.sizeCode,
            senderName = detail.senderName,
            shipmentType = detail.shipmentType,
        )
        when (result) {
            is Result.Loading -> base.copy(isLoading = true)
            is Result.Success -> base.copy(parcel = result.data, isLoading = false)
            is Result.Error -> base.copy(
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
