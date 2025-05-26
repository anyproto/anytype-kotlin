package com.anytypeio.anytype.domain.deeplink

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SavePendingDeeplinkTest {

    @Mock
    private lateinit var userSettingsRepository: UserSettingsRepository

    @Mock
    private lateinit var deepLinkResolver: DeepLinkResolver

    @Mock
    private lateinit var logger: Logger

    private val testDispatcher = TestCoroutineDispatcher()
    private val dispatchers = AppCoroutineDispatchers(
        io = testDispatcher,
        main = testDispatcher,
        computation = testDispatcher
    )

    private lateinit var savePendingDeeplink: SavePendingDeeplink

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        savePendingDeeplink = SavePendingDeeplink(
            userSettingsRepository = userSettingsRepository,
            deepLinkResolver = deepLinkResolver,
            logger = logger,
            dispatchers = dispatchers
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `should save invite deeplink and return true`() = runTest {
        // Given
        val deeplink = "https://invite.any.coop/bafybeihkgo4vrp22rqaj2lppo3m3xqcm7tjh2otwnch7opgavq26go44fa#2hQH3vMvhrfewAYxAtZFSRN2jTJSgCPtKC7gUYExnXg8"
        val action = DeepLinkResolver.Action.Invite(deeplink)
        Mockito.`when`(deepLinkResolver.resolve(deeplink)).thenReturn(action)

        // When
        val result = savePendingDeeplink.async(SavePendingDeeplink.Params(deeplink)).getOrNull()

        // Then
        assertTrue(result == true)
        Mockito.verify(logger).logInfo("Saving pending deeplink: $deeplink")
        Mockito.verify(logger).logInfo("Deeplink is an invite, saving to user settings")
        Mockito.verify(userSettingsRepository).setPendingInviteDeeplink(deeplink)
        Mockito.verifyNoMoreInteractions(userSettingsRepository)
    }

    @Test
    fun `should not save non-invite deeplink and return false`() = runTest {
        // Given
        val deeplink = "anytype://object?objectId=123&spaceId=456"
        val action = DeepLinkResolver.Action.DeepLinkToObject(
            obj = "123",
            space = SpaceId("456")
        )
        Mockito.`when`(deepLinkResolver.resolve(deeplink)).thenReturn(action)

        // When
        val result = savePendingDeeplink.async(SavePendingDeeplink.Params(deeplink)).getOrNull()

        // Then
        assertFalse(result == true)
        Mockito.verify(logger).logInfo("Saving pending deeplink: $deeplink")
        Mockito.verify(logger).logInfo("Deeplink is not an invite, not saving")
        Mockito.verifyNoMoreInteractions(userSettingsRepository)
    }

    @Test
    fun `should not save unknown deeplink and return false`() = runTest {
        // Given
        val deeplink = "anytype://unknown/path"
        Mockito.`when`(deepLinkResolver.resolve(deeplink)).thenReturn(DeepLinkResolver.Action.Unknown)

        // When
        val result = savePendingDeeplink.async(SavePendingDeeplink.Params(deeplink)).getOrNull()

        // Then
        assertFalse(result == true)
        Mockito.verify(logger).logInfo("Saving pending deeplink: $deeplink")
        Mockito.verify(logger).logInfo("Deeplink is not an invite, not saving")
        Mockito.verifyNoMoreInteractions(userSettingsRepository)
    }

    @Test
    fun `should handle empty deeplink`() = runTest {
        // Given
        val deeplink = ""
        Mockito.`when`(deepLinkResolver.resolve(deeplink)).thenReturn(DeepLinkResolver.Action.Unknown)

        // When
        val result = savePendingDeeplink.async(SavePendingDeeplink.Params(deeplink)).getOrNull()

        // Then
        assertFalse(result == true)
        Mockito.verify(logger).logInfo("Saving pending deeplink: $deeplink")
        Mockito.verify(logger).logInfo("Deeplink is not an invite, not saving")
        Mockito.verifyNoMoreInteractions(userSettingsRepository)
    }

    @Test
    fun `should handle malformed deeplink`() = runTest {
        // Given
        val deeplink = "invalid://deeplink"
        Mockito.`when`(deepLinkResolver.resolve(deeplink)).thenReturn(DeepLinkResolver.Action.Unknown)

        // When
        val result = savePendingDeeplink.async(SavePendingDeeplink.Params(deeplink)).getOrNull()

        // Then
        assertFalse(result == true)
        Mockito.verify(logger).logInfo("Saving pending deeplink: $deeplink")
        Mockito.verify(logger).logInfo("Deeplink is not an invite, not saving")
        Mockito.verifyNoMoreInteractions(userSettingsRepository)
    }
}