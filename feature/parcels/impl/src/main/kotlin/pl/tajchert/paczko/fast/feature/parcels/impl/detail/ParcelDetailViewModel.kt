package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.common.result.Result
import pl.tajchert.paczko.fast.core.common.result.asResult
import pl.tajchert.paczko.fast.core.domain.GetParcelDetailsUseCase
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails

@HiltViewModel(assistedFactory = ParcelDetailViewModel.Factory::class)
class ParcelDetailViewModel @AssistedInject constructor(
    @Assisted private val shipmentNumber: String,
    observeParcel: ObserveParcelUseCase,
    getParcelDetails: GetParcelDetailsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shipmentNumber: String): ParcelDetailViewModel
    }

    private val details = MutableStateFlow(ParcelDetails())

    init {
        viewModelScope.launch {
            details.value = runCatching { getParcelDetails(shipmentNumber) }.getOrDefault(ParcelDetails())
        }
    }

    val uiState: StateFlow<ParcelDetailUiState> = combine(
        observeParcel(shipmentNumber).asResult(),
        details,
    ) { result, detail ->
        val base = ParcelDetailUiState(
            events = detail.events,
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
