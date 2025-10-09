package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InitializeAppInstallationDataTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    private val dispatchers = AppCoroutineDispatchers(
        io = rule.dispatcher,
        computation = rule.dispatcher,
        main = rule.dispatcher
    )

    @Mock
    lateinit var userSettingsRepository: UserSettingsRepository

    private lateinit var useCase: InitializeAppInstallationData

    private val testAccount = Account(id = "test-account-123")
    private val testVersion = "1.0.0"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this).close()
        useCase = InitializeAppInstallationData(
            userSettingsRepository = userSettingsRepository,
            dispatchers = dispatchers
        )
    }

    // ==================== Fresh Install Tests ====================

    @Test
    fun `GIVEN installedAtDate is null WHEN use case runs THEN should return isFirstLaunch true`() = runTest {
        // Given
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(null)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = testVersion
            )
        )

        // Then
        assertTrue(result.isFirstLaunch)
    }

    @Test
    fun `GIVEN fresh install WHEN use case runs THEN previousVersion should be null`() = runTest {
        // Given
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(null)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = testVersion
            )
        )

        // Then
        assertNull(result.previousVersion)
    }

    @Test
    fun `GIVEN fresh install WHEN use case runs THEN installedAtTimestamp should be recent`() = runTest {
        // Given
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(null)

        val beforeExecution = System.currentTimeMillis()

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = testVersion
            )
        )

        val afterExecution = System.currentTimeMillis()

        // Then
        assertTrue(result.installedAtTimestamp >= beforeExecution)
        assertTrue(result.installedAtTimestamp <= afterExecution)
    }

    // ==================== Existing User Tests ====================

    @Test
    fun `GIVEN installedAtDate exists WHEN use case runs THEN should return isFirstLaunch false`() = runTest {
        // Given
        val existingTimestamp = 1704067200000L // Jan 1, 2024
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(existingTimestamp)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn("0.40.13")

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = testVersion
            )
        )

        // Then
        assertFalse(result.isFirstLaunch)
    }

    @Test
    fun `GIVEN existing user WHEN use case runs THEN should preserve original installedAtDate`() = runTest {
        // Given
        val existingTimestamp = 1704067200000L
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(existingTimestamp)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn("0.40.13")

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = testVersion
            )
        )

        // Then
        assertEquals(existingTimestamp, result.installedAtTimestamp)
    }

    // ==================== Version Update Tests ====================

    @Test
    fun `GIVEN version changed WHEN use case runs THEN should return old version as previousVersion`() = runTest {
        // Given
        val oldVersion = "0.40.13"
        val newVersion = "0.41.0"
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(1704067200000L)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(oldVersion)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = newVersion
            )
        )

        // Then
        assertEquals(oldVersion, result.previousVersion)
        assertEquals(newVersion, result.currentVersion)
    }

    @Test
    fun `GIVEN version unchanged WHEN use case runs THEN should return same version`() = runTest {
        // Given
        val sameVersion = "1.0.0"
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(1704067200000L)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(sameVersion)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = sameVersion
            )
        )

        // Then
        assertEquals(sameVersion, result.currentVersion)
        assertEquals(sameVersion, result.previousVersion)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `GIVEN multiple accounts WHEN use case runs THEN should track separately`() = runTest {
        // Given
        val account1 = Account(id = "account-1")
        val account2 = Account(id = "account-2")

        whenever(userSettingsRepository.getInstalledAtDate(account1)).thenReturn(null)
        whenever(userSettingsRepository.getInstalledAtDate(account2)).thenReturn(1704067200000L)
        whenever(userSettingsRepository.getCurrentAppVersion(account1)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(account2)).thenReturn("0.40.0")

        // When
        val result1 = useCase.run(
            InitializeAppInstallationData.Params(
                account = account1,
                currentAppVersion = testVersion
            )
        )

        val result2 = useCase.run(
            InitializeAppInstallationData.Params(
                account = account2,
                currentAppVersion = testVersion
            )
        )

        // Then
        assertTrue(result1.isFirstLaunch, "Account 1 should be first launch")
        assertFalse(result2.isFirstLaunch, "Account 2 should not be first launch")
    }

    @Test
    fun `GIVEN version with different formats WHEN use case runs THEN should handle correctly`() = runTest {
        // Given
        val oldVersion = "0.40.13-beta"
        val newVersion = "1.0.0"
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(1704067200000L)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(oldVersion)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = newVersion
            )
        )

        // Then
        assertEquals(oldVersion, result.previousVersion)
        assertEquals(newVersion, result.currentVersion)
    }

    @Test
    fun `GIVEN empty version string WHEN use case runs THEN should handle gracefully`() = runTest {
        // Given
        val emptyVersion = ""
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(null)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = emptyVersion
            )
        )

        // Then
        assertTrue(result.isFirstLaunch)
        assertEquals(emptyVersion, result.currentVersion)
    }

    // ==================== Complete Scenario Tests ====================

    @Test
    fun `SCENARIO fresh install - complete data flow`() = runTest {
        // Given - Completely new user
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(null)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn(null)

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = "1.0.0"
            )
        )

        // Then
        assertTrue(result.isFirstLaunch, "Should be first launch")
        assertEquals("1.0.0", result.currentVersion)
        assertNull(result.previousVersion, "No previous version for fresh install")
        assertNotNull(result.installedAtTimestamp)
    }

    @Test
    fun `SCENARIO existing user updates version - complete data flow`() = runTest {
        // Given - User had v0.40.13, updating to v0.41.0
        val originalInstallTime = 1704067200000L // Jan 1, 2024
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(originalInstallTime)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn("0.40.13")

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = "0.41.0"
            )
        )

        // Then
        assertFalse(result.isFirstLaunch, "Should not be first launch")
        assertEquals("0.41.0", result.currentVersion)
        assertEquals("0.40.13", result.previousVersion)
        assertEquals(originalInstallTime, result.installedAtTimestamp, "Should preserve original install time")
    }

    @Test
    fun `SCENARIO existing user reopens app same version - complete data flow`() = runTest {
        // Given - User already on v1.0.0, just reopening app
        val originalInstallTime = 1704067200000L
        whenever(userSettingsRepository.getInstalledAtDate(testAccount)).thenReturn(originalInstallTime)
        whenever(userSettingsRepository.getCurrentAppVersion(testAccount)).thenReturn("1.0.0")

        // When
        val result = useCase.run(
            InitializeAppInstallationData.Params(
                account = testAccount,
                currentAppVersion = "1.0.0"
            )
        )

        // Then
        assertFalse(result.isFirstLaunch)
        assertEquals("1.0.0", result.currentVersion)
        assertEquals("1.0.0", result.previousVersion, "Previous version should match current")
        assertEquals(originalInstallTime, result.installedAtTimestamp)
    }
}
