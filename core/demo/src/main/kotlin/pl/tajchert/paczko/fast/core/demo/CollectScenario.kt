package pl.tajchert.paczko.fast.core.demo

/** The locker outcome a demo parcel triggers when collected. */
enum class CollectScenario {
    Success,
    SessionExpired,
    InvalidSessionState,
    InvalidCompartmentState,
    InvalidParcelState,
    CannotFindCompartment,
    BoxMachineNotFound,
    InvalidSession,
    Unknown,
    SlowClose,
    PostOpenSoftFail,
}

/** Maps a demo shipment number to its wired locker scenario (default success). */
fun scenarioFor(shipmentNumber: String): CollectScenario = when (shipmentNumber) {
    DemoData.READY_SESSION_EXPIRED -> CollectScenario.SessionExpired
    DemoData.READY_BOX_OFFLINE -> CollectScenario.BoxMachineNotFound
    DemoData.READY_SLOW_CLOSE -> CollectScenario.SlowClose
    DemoData.READY_POST_OPEN_FAIL -> CollectScenario.PostOpenSoftFail
    else -> CollectScenario.Success
}
