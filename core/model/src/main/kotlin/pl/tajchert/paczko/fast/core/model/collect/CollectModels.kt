package pl.tajchert.paczko.fast.core.model.collect

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
)

enum class ExpectedCompartmentStatus {
    OPENED,
    CLOSED,
}

sealed interface CollectState {
    data object Idle : CollectState
    data object Validating : CollectState
    data class Opening(val sessionUuid: String) : CollectState
    data class WaitingForOpened(val sessionUuid: String) : CollectState
    data class Opened(val sessionUuid: String) : CollectState
    data class WaitingForClosed(val sessionUuid: String) : CollectState
    data class ConfirmingClosed(val sessionUuid: String) : CollectState
    data class Claiming(val sessionUuid: String) : CollectState
    data object Completed : CollectState
    data class Failed(
        val message: String,
        val canRetryFromValidation: Boolean,
        /**
         * True when the compartment had already opened before this failure. The
         * parcel is physically collectable, so the UI surfaces such failures as a
         * snackbar over the collected screen rather than a full-screen error.
         */
        val boxAlreadyOpen: Boolean = false,
    ) : CollectState
    data object Canceled : CollectState
}

enum class CollectErrorCode(val apiValue: String, val canRestartValidation: Boolean) {
    InvalidSession("invalidSession", false),
    SessionExpired("sessionExpired", true),
    InvalidSessionState("invalidSessionState", true),
    InvalidCompartmentState("invalidCompartmentState", false),
    InvalidParcelState("invalidParcelState", false),
    CannotFindCompartment("cannotFindCompartment", false),
    BoxMachineNotFound("boxMachineNotFound", false),
    Unknown("unknown", false);

    companion object {
        fun fromApiValue(value: String?): CollectErrorCode =
            entries.firstOrNull { it.apiValue == value } ?: Unknown
    }
}
