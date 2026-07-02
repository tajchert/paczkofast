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
}
