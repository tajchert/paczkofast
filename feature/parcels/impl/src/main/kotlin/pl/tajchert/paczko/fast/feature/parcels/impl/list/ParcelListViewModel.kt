package pl.tajchert.paczko.fast.feature.parcels.impl.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.common.result.Result
import pl.tajchert.paczko.fast.core.common.result.asResult
import pl.tajchert.paczko.fast.core.domain.ObserveParcelsUseCase
import pl.tajchert.paczko.fast.core.domain.RefreshParcelsUseCase
import javax.inject.Inject

@HiltViewModel
class ParcelListViewModel @Inject constructor(
    observeParcels: ObserveParcelsUseCase,
    private val refreshParcels: RefreshParcelsUseCase,
) : ViewModel() {

    private val refreshState = MutableStateFlow(RefreshState())

    val uiState: StateFlow<ParcelListUiState> = combine(
        observeParcels().asResult(),
        refreshState,
    ) { parcelsResult, refreshState ->
        when (parcelsResult) {
            is Result.Loading -> ParcelListUiState(
                isRefreshing = refreshState.isRefreshing,
                errorMessage = refreshState.errorMessage,
            )
            is Result.Success -> ParcelListUiState(
                parcels = parcelsResult.data,
                isRefreshing = refreshState.isRefreshing,
                errorMessage = refreshState.errorMessage,
            )
            is Result.Error -> ParcelListUiState(
                isRefreshing = refreshState.isRefreshing,
                errorMessage = parcelsResult.exception.message ?: "Unable to load parcels",
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ParcelListUiState(),
    )

    fun refresh() {
        viewModelScope.launch {
            refreshState.value = RefreshState(isRefreshing = true)
            refreshState.value = try {
                refreshParcels()
                RefreshState(isRefreshing = false)
            } catch (exception: Exception) {
                RefreshState(
                    isRefreshing = false,
                    errorMessage = exception.message ?: "Unable to refresh parcels",
                )
            }
        }
    }
}

private data class RefreshState(
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
