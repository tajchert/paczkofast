# Paczkofast

Paczkofast is a small native Android app for fast InPost parcel checking and Paczkomat collection.

## Scope

- SMS login
- Tracked parcel list
- Parcel status and pickup details
- Pickup QR rendering from `qrCode`
- Remote collect flow: validate, open, opened status, closed status, closed confirmation, claim

## Build

```bash
./gradlew :app:compileDebugKotlin
./gradlew test
```

## API Note

The InPost Mobile API used by this app is unofficial and may change without notice.
