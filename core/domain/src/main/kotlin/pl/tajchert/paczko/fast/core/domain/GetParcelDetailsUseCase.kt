package pl.tajchert.paczko.fast.core.domain

import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import pl.tajchert.paczko.fast.core.model.parcel.ParcelDetails
import javax.inject.Inject

class GetParcelDetailsUseCase @Inject constructor(
    private val repository: ParcelRepository,
) {
    suspend operator fun invoke(shipmentNumber: String): ParcelDetails =
        repository.getParcelDetails(shipmentNumber)
}
