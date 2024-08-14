package com.anytypeio.anytype.domain.subscriptions

import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
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
        private val spaceSyncAndP2PStatusProviderImpl: SpaceSyncAndP2PStatusProvider
    ) : GlobalSubscriptionManager {

        override fun onStart() {
            types.onStart()
            relations.onStart()
            permissions.start()
            isSpaceDeleted.onStart()
            spaceSyncAndP2PStatusProviderImpl.onStart()
        }

        override fun onStop() {
            types.onStop()
            relations.onStop()
            permissions.stop()
            isSpaceDeleted.onStop()
            spaceSyncAndP2PStatusProviderImpl.onStop()
        }
    }

}