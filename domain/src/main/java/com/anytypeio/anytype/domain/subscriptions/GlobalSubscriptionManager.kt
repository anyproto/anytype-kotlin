package com.anytypeio.anytype.domain.subscriptions

import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.domain.device.NetworkConnectionStatus
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.PushKeyProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import javax.inject.Inject

interface GlobalSubscriptionManager {

    fun onStart()
    fun onStop()

    class Default @Inject constructor(
        private val types: ObjectTypesSubscriptionManager,
        private val relations: RelationsSubscriptionManager,
        private val permissions: UserPermissionProvider,
        private val isSpaceDeleted: SpaceDeletedStatusWatcher,
        private val profile: ProfileSubscriptionManager,
        private val networkConnectionStatus: NetworkConnectionStatus,
        private val deviceTokenStoringService: DeviceTokenStoringService,
        private val pushKeyProvider: PushKeyProvider,
        private val logger: Logger
    ) : GlobalSubscriptionManager {

        override fun onStart() {
            logger.logInfo("GlobalSubscriptionManager: Starting all subscriptions")
            pushKeyProvider.start()
            types.onStart()
            relations.onStart()
            permissions.start()
            isSpaceDeleted.onStart()
            profile.onStart()
            networkConnectionStatus.start()
            deviceTokenStoringService.start()
        }

        override fun onStop() {
            logger.logInfo("GlobalSubscriptionManager: Stopping all subscriptions")
            pushKeyProvider.stop()
            types.onStop()
            relations.onStop()
            permissions.stop()
            isSpaceDeleted.onStop()
            profile.onStop()
            networkConnectionStatus.stop()
            deviceTokenStoringService.stop()
        }
    }

}