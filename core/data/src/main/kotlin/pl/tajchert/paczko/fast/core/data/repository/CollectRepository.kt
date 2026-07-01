package pl.tajchert.paczko.fast.core.data.repository

import pl.tajchert.paczko.fast.core.model.collect.ExpectedCompartmentStatus
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

interface CollectRepository {
    suspend fun validate(shipmentNumber: String, openCode: String, geoPoint: GeoPoint): String
    suspend fun open(sessionUuid: String)
    suspend fun pollStatus(sessionUuid: String, expectedStatus: ExpectedCompartmentStatus)
    suspend fun closed(sessionUuid: String)
    suspend fun claim(sessionUuid: String, shipmentNumber: String)
}
