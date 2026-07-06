package pl.tajchert.paczko.fast.core.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.datastore.AuthTokensDataSource
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber

class DemoAuthRepositoryTest {

    private class FakeAuthTokensDataSource : AuthTokensDataSource {
        val session = MutableStateFlow(AuthSession("", ""))
        val phone = MutableStateFlow<String?>(null)
        override val authSession: Flow<AuthSession> = session
        override val phoneNumber: Flow<String?> = phone
        override suspend fun saveTokens(authToken: String, refreshToken: String) {
            session.value = AuthSession(authToken, refreshToken)
        }
        override suspend fun savePhoneNumber(phoneNumber: String) { phone.value = phoneNumber }
        override suspend fun clearTokens() {
            session.value = AuthSession("", "")
            phone.value = null
        }
    }

    @Test
    fun `confirmSmsCode authenticates and stores phone`() = runTest {
        val tokens = FakeAuthTokensDataSource()
        val repo = DemoAuthRepository(tokens)

        val result = repo.confirmSmsCode(PhoneNumber(prefix = "48", value = "500100200"), smsCode = "000000")

        assertTrue(result.isAuthenticated)
        assertTrue(repo.observeAuthSession().first().isAuthenticated)
        assertEquals("48500100200", repo.observePhoneNumber().first())
    }

    @Test
    fun `logout clears the session`() = runTest {
        val tokens = FakeAuthTokensDataSource()
        val repo = DemoAuthRepository(tokens)
        repo.confirmSmsCode(PhoneNumber("48", "500100200"), "000000")

        repo.logout()

        assertFalse(repo.observeAuthSession().first().isAuthenticated)
        assertNull(repo.observePhoneNumber().first())
    }
}
