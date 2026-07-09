package pl.tajchert.paczko.fast.core.data.repository

/**
 * A collect endpoint replied 2xx but the body could not be parsed — the
 * unofficial API returned something outside the expected contract. The message
 * is deliberately human-readable because [CollectState.Failed][pl.tajchert.paczko.fast.core.model.collect.CollectState.Failed]
 * surfaces it on the error screen verbatim; the raw parser error stays in [cause].
 */
class CollectUnexpectedResponseException(
    cause: Throwable,
) : RuntimeException("Unexpected response from the locker service", cause)
