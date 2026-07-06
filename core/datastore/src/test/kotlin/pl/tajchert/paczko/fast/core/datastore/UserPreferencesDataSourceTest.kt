package pl.tajchert.paczko.fast.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.ParcelListOpenButtonMode
import java.io.File

class UserPreferencesDataSourceTest {

    private lateinit var tempFile: File
    private lateinit var dataSource: UserPreferencesDataSource

    @Before
    fun setUp() {
        tempFile = File.createTempFile("user_preferences_test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tempFile },
        )
        dataSource = UserPreferencesDataSource(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun hasSeenOnboarding_defaultsToFalse() = runTest {
        val preferences = dataSource.userPreferences.first()

        assertFalse(preferences.hasSeenOnboarding)
    }

    @Test
    fun hasSeenOnboarding_emitsTrueAfterSet() = runTest {
        dataSource.setHasSeenOnboarding(true)

        val preferences = dataSource.userPreferences.first()

        assertTrue(preferences.hasSeenOnboarding)
    }

    @Test
    fun parcelListOpenButtonMode_defaultsToFirst() = runTest {
        val preferences = dataSource.userPreferences.first()

        assertEquals(ParcelListOpenButtonMode.FIRST, preferences.parcelListOpenButtonMode)
    }

    @Test
    fun parcelListOpenButtonMode_emitsStoredValueAfterSet() = runTest {
        dataSource.setParcelListOpenButtonMode(ParcelListOpenButtonMode.ALL)

        val preferences = dataSource.userPreferences.first()

        assertEquals(ParcelListOpenButtonMode.ALL, preferences.parcelListOpenButtonMode)
    }
}
