package pl.tajchert.paczko.fast.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class InpostDtoSerializationTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun confirmSmsRequestUsesAppPhoneAndPlatformFormat() {
        val body = json.encodeToString(
            ConfirmSmsRequestDto(
                phoneNumber = PhoneNumberDto(prefix = "+48", value = "600123456"),
                smsCode = "1234",
                devicePlatform = "Android",
            ),
        )

        assertEquals(
            """{"phoneNumber":{"prefix":"+48","value":"600123456"},"smsCode":"1234","devicePlatform":"Android"}""",
            body,
        )
    }

    @Test
    fun trackedParcelsResponseKeepsQrCodeAndOpenCodeSeparate() {
        val response = json.decodeFromString<TrackedParcelsResponseDto>(
            """
            {
              "parcels": [{
                "shipmentNumber": "111",
                "shipmentType": "BOX",
                "status": "ready_to_pickup",
                "statusGroup": "ready",
                "openCode": "123456",
                "qrCode": "opaque-qr",
                "operations": { "collect": true },
                "events": [],
                "eventLog": [],
                "sharedTo": [],
                "ownershipStatus": "OWNER"
              }],
              "removedParcelList": ["222"],
              "more": false
            }
            """.trimIndent(),
        )

        assertEquals("123456", response.parcels.single().openCode)
        assertEquals("opaque-qr", response.parcels.single().qrCode)
        assertEquals(true, response.parcels.single().operations.collect)
        assertEquals(listOf("222"), response.removedParcelList)
    }

    @Test
    fun trackedParcelsResponseKeepsMultiPackageAndOwnershipMetadata() {
        val response = json.decodeFromString<TrackedParcelsResponseDto>(
            """
            {
              "parcels": [{
                "shipmentNumber": "111",
                "status": "ready_to_pickup",
                "multiCompartment": {
                  "uuid": "multi-uuid",
                  "shipmentNumbers": ["111", "222"]
                },
                "ownershipStatus": "SHARED_TO_ME",
                "operations": { "collect": true }
              }],
              "more": false
            }
            """.trimIndent(),
        )

        val parcel = response.parcels.single()
        assertEquals("multi-uuid", parcel.multiCompartment?.uuid)
        assertEquals(listOf("111", "222"), parcel.multiCompartment?.shipmentNumbers)
        assertEquals("SHARED_TO_ME", parcel.ownershipStatus)
    }

    // -------------------------------------------------------------------------
    // Collect flow wire contract. These bodies drive a physical locker, so a
    // silent field-name change would break collection with no compile error.
    // Shapes mirror the real requests captured in OkHttp logs (fake values).
    // -------------------------------------------------------------------------

    @Test
    fun validateRequestSerializesParcelAndGeoPoint() {
        val body = json.encodeToString(
            CollectValidateRequestDto(
                parcel = ParcelCompartmentDto(
                    shipmentNumber = "000000000000000000000000",
                    openCode = "000000",
                ),
                geoPoint = GeoPointDto(latitude = 52.0, longitude = 21.0, accuracy = 10.0),
            ),
        )

        assertEquals(
            """{"parcel":{"shipmentNumber":"000000000000000000000000","openCode":"000000"},""" +
                """"geoPoint":{"latitude":52.0,"longitude":21.0,"accuracy":10.0}}""",
            body,
        )
    }

    @Test
    fun statusRequestSerializesExpectedStatus() {
        val body = json.encodeToString(
            CollectStatusRequestDto(sessionUuid = "session", expectedStatus = "CLOSED"),
        )

        assertEquals("""{"sessionUuid":"session","expectedStatus":"CLOSED"}""", body)
    }

    @Test
    fun claimRequestSerializesShipmentNumbersArray() {
        val body = json.encodeToString(
            CollectClaimRequestDto(
                sessionUuid = "session",
                shipmentNumbers = listOf("111", "222"),
            ),
        )

        assertEquals("""{"sessionUuid":"session","shipmentNumbers":["111","222"]}""", body)
    }

    @Test
    fun validateResponseReadsSessionUuid() {
        val response = json.decodeFromString<CollectValidateResponseDto>(
            """{"sessionUuid":"session-abc"}""",
        )

        assertEquals("session-abc", response.sessionUuid)
    }

    @Test
    fun errorResponseDecodesApiErrorCode() {
        val error = json.decodeFromString<ErrorResponseDto>(
            """{"error":"sessionExpired","status":400,"description":"expired"}""",
        )

        assertEquals("sessionExpired", error.error)
    }

    @Test
    fun compartmentResponseToleratesUnknownAndMissingFields() {
        // The status endpoint returns a body we don't consume; unknown/new fields
        // must not break decoding (mirrors the v4 phantom-field lesson).
        val response = json.decodeFromString<CompartmentResponseDto>(
            """{"status":"CLOSED","someFutureField":true,"actionTime":1234}""",
        )

        assertEquals("CLOSED", response.status)
        assertEquals(1234L, response.actionTime)
    }
}
