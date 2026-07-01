package pl.tajchert.paczko.fast.core.data.repository

class CollectApiException(
    val apiValue: String,
) : RuntimeException(apiValue)
