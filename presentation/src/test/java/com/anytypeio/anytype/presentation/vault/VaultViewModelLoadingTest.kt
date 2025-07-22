package com.anytypeio.anytype.presentation.vault

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.chats.VaultChatPreviewContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelLoadingTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    private lateinit var spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    private lateinit var chatPreviewContainer: VaultChatPreviewContainer
    private lateinit var userPermissionProvider: UserPermissionProvider
    private lateinit var notificationPermissionManager: NotificationPermissionManager
    private lateinit var stringResourceProvider: StringResourceProvider

    @Before
    fun setup() {
        spaceViewSubscriptionContainer = mock()
        chatPreviewContainer = mock()
        userPermissionProvider = mock()
        notificationPermissionManager = mock()
        stringResourceProvider = mock()
    }

    @Test
    fun `should show loading only for initial emission and not for subsequent emissions`() = runTest {
        // Given - start with empty flows to ensure first emission triggers loading
        val spaceViewsFlow = MutableStateFlow(emptyList<ObjectWrapper.SpaceView>())
        val chatPreviewsFlow = MutableStateFlow(emptyList<Chat.Preview>())
        val permissions = emptyMap<String, SpaceMemberPermissions>()
        val permissionState = NotificationPermissionManagerImpl.PermissionState.Granted

        whenever(spaceViewSubscriptionContainer.observe()).thenReturn(spaceViewsFlow)
        whenever(chatPreviewContainer.observePreviewsWithAttachments()).thenReturn(chatPreviewsFlow)
        whenever(userPermissionProvider.all()).thenReturn(flowOf(permissions))
        whenever(notificationPermissionManager.permissionState()).thenReturn(MutableStateFlow(permissionState))
        whenever(notificationPermissionManager.areNotificationsEnabled()).thenReturn(true)
        whenever(stringResourceProvider.getSpaceAccessTypeName(any())).thenReturn("Private")

        // When - create ViewModel
        val viewModel = VaultViewModelFabric.create(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            chatPreviewContainer = chatPreviewContainer,
            userPermissionProvider = userPermissionProvider,
            notificationPermissionManager = notificationPermissionManager,
            stringResourceProvider = stringResourceProvider
        )

        // Then - loading should be true immediately after ViewModel creation
        assertTrue("Loading should be true immediately after ViewModel creation", viewModel.loadingState.value)
        
        // Now trigger the first emission by providing data to chat previews (which drives the flow)
        chatPreviewsFlow.value = listOf(createTestChatPreview("chat1", "space1"))
        
        // Advance just a little to start processing but not complete the delay
        advanceTimeBy(50) // Less than INITIAL_LOADING_DELAY_MS (200ms)
        
        // During the delay, loading should still be true
        assertTrue("Loading should still be true during first emission delay", viewModel.loadingState.value)
        
        // Complete the delay and finish processing
        advanceTimeBy(150) // Complete the 200ms delay
        advanceUntilIdle()

        // After the delay, loading should be false
        assertFalse("Loading should be false after first emission delay", viewModel.loadingState.value)
        assertEquals("Should have 0 spaces with empty space flow", 0, viewModel.sections.value.mainSpaces.size)
        
        // Now provide space data to complete the scenario
        spaceViewsFlow.value = listOf(createTestSpaceWrapper("space1"))
        
        // Then - loading should remain false for subsequent emissions
        assertFalse("Loading should remain false for subsequent emission", viewModel.loadingState.value)
        advanceUntilIdle()
        assertFalse("Loading should still be false after processing", viewModel.loadingState.value)
        assertEquals("Should have 1 space after space data provided", 1, viewModel.sections.value.mainSpaces.size)
        
        // When - trigger another data change
        spaceViewsFlow.value = listOf(createTestSpaceWrapper("space1"), createTestSpaceWrapper("space2"))
        
        // Then - loading should still remain false
        assertFalse("Loading should remain false for third emission", viewModel.loadingState.value)
        advanceUntilIdle()
        assertFalse("Loading should remain false after third emission processed", viewModel.loadingState.value)
        assertEquals("Should have 2 spaces after third emission", 2, viewModel.sections.value.mainSpaces.size)
    }

    private fun createTestSpaceWrapper(id: String): ObjectWrapper.SpaceView {
        return StubSpaceView(
            id = id,
            targetSpaceId = id,
            spaceAccessType = SpaceAccessType.DEFAULT,
            spaceAccountStatus = SpaceStatus.OK,
            spaceLocalStatus = SpaceStatus.OK,
            chatId = "",
            spaceUxType = SpaceUxType.DATA,
            createdDate = System.currentTimeMillis().toDouble()
        )
    }
    
    private fun createTestChatPreview(chatId: String, spaceId: String): Chat.Preview {
        return Chat.Preview(
            chat = chatId,
            space = SpaceId(spaceId),
            message = null, // No message for testing, just a preview placeholder
            dependencies = emptyList(),
            state = null
        )
    }
}