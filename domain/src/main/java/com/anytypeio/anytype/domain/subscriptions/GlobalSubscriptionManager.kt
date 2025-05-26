package com.anytypeio.anytype.domain.subscriptions

import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.deeplink.PendingDeeplinkProcessor
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface GlobalSubscriptionManager {

    fun onStart()
    fun onStop()
    
    /**
     * Observable for pending deeplink actions that were processed when authentication was established.
     */
    fun observePendingDeeplinks(): Flow<DeepLinkResolver.Action>

    class Default @Inject constructor(
        private val types: ObjectTypesSubscriptionManager,
        private val relations: RelationsSubscriptionManager,
        private val permissions: UserPermissionProvider,
        private val isSpaceDeleted: SpaceDeletedStatusWatcher,
        private val profile: ProfileSubscriptionManager,
        private val networkConnectionStatus: NetworkConnectionStatus,
        private val deviceTokenStoringService: DeviceTokenStoringService,
        private val chatPreviewContainer: ChatPreviewContainer,
        private val pendingDeeplinkProcessor: PendingDeeplinkProcessor,
        private val scope: CoroutineScope
    ) : GlobalSubscriptionManager {

        override fun onStart() {
            types.onStart()
            relations.onStart()
            permissions.start()
            isSpaceDeleted.onStart()
            profile.onStart()
            networkConnectionStatus.start()
            deviceTokenStoringService.start()
            chatPreviewContainer.start()
            
            // Process any pending deeplinks now that authentication is established
            scope.launch {
                pendingDeeplinkProcessor.processPendingDeeplinks().collect { 
                    // The deeplink will be emitted through observePendingDeeplinks()
                }
            }
        }

        override fun onStop() {
            types.onStop()
            relations.onStop()
            permissions.stop()
            isSpaceDeleted.onStop()
            profile.onStop()
            networkConnectionStatus.stop()
            deviceTokenStoringService.stop()
            chatPreviewContainer.stop()
        }
        
        override fun observePendingDeeplinks(): Flow<DeepLinkResolver.Action> {
            return pendingDeeplinkProcessor.processPendingDeeplinks()
        }
    }

}