package pl.tajchert.paczko.fast.feature.parcels.impl.list

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.model.parcel.Parcel

/**
 * [ImmutableList] + [Immutable] keep this state stable to the Compose compiler
 * so consumers can skip recomposition when it is unchanged. A plain `List` is
 * treated as unstable (and `Parcel` comes from a non-Compose module, so it is
 * unstable too), which would force recomposition whenever the holder recomposes.
 */
@Immutable
data class ParcelListUiState(
    val parcels: ImmutableList<Parcel> = persistentListOf(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** First load with no cached parcels yet — show a full-screen spinner. */
    val isLoading: Boolean = false,
)
