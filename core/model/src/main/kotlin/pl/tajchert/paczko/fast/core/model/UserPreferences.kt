package pl.tajchert.paczko.fast.core.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hasSeenOnboarding: Boolean = false,
    val lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD,
)
