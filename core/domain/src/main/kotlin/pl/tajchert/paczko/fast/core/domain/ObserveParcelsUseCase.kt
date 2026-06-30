package pl.tajchert.paczko.fast.core.domain

import pl.tajchert.paczko.fast.core.data.repository.ParcelRepository
import javax.inject.Inject

class ObserveParcelsUseCase @Inject constructor(
    private val repository: ParcelRepository,
) {
    operator fun invoke() = repository.observeParcels()
}
