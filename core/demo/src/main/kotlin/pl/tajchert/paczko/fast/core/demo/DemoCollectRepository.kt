package pl.tajchert.paczko.fast.core.demo

import pl.tajchert.paczko.fast.core.data.repository.CollectRepository
import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint
import javax.inject.Inject

class DemoCollectRepository @Inject constructor() : CollectRepository {
    override suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String = "demo-session-uuid"
    override suspend fun open(sessionUuid: String) = Unit
    override suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus) = Unit
    override suspend fun closed(sessionUuid: String) = Unit
    override suspend fun claim(sessionUuid: String, shipmentNumbers: List<String>) = Unit
}
