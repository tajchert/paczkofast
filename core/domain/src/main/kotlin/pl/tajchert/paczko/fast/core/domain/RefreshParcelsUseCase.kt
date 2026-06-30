package pl.tajchert.paczko.fast.core.domain

import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import javax.inject.Inject

class RefreshParcelsUseCase @Inject constructor(
    private val repository: ParcelRepository,
) {
    suspend operator fun invoke() = repository.refreshTrackedParcels()
}
