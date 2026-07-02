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
import pl.tajchert.paczko.fast.core.domain.GetTrackingEventsUseCase
import pl.tajchert.paczko.fast.core.domain.ObserveParcelUseCase
import pl.tajchert.paczko.fast.core.model.parcel.TrackingEvent

@HiltViewModel(assistedFactory = ParcelDetailViewModel.Factory::class)
class ParcelDetailViewModel @AssistedInject constructor(
    @Assisted private val shipmentNumber: String,
    observeParcel: ObserveParcelUseCase,
    getTrackingEvents: GetTrackingEventsUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(shipmentNumber: String): ParcelDetailViewModel
    }

    private val events = MutableStateFlow<List<TrackingEvent>>(emptyList())

    init {
        viewModelScope.launch {
            events.value = runCatching { getTrackingEvents(shipmentNumber) }.getOrDefault(emptyList())
        }
    }

    val uiState: StateFlow<ParcelDetailUiState> = combine(
        observeParcel(shipmentNumber).asResult(),
        events,
    ) { result, trackingEvents ->
        when (result) {
            is Result.Loading -> ParcelDetailUiState(isLoading = true, events = trackingEvents)
            is Result.Success -> ParcelDetailUiState(
                parcel = result.data,
                isLoading = false,
                events = trackingEvents,
            )
            is Result.Error -> ParcelDetailUiState(
                isLoading = false,
                errorMessage = result.exception.message ?: "Unable to load parcel",
                events = trackingEvents,
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ParcelDetailUiState(isLoading = true),
        )
}
