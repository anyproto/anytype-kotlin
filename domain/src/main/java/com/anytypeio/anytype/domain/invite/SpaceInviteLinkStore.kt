package com.anytypeio.anytype.domain.invite

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Store for managing space invite link access levels across the application.
 * Provides reactive updates to all subscribed screens when invite link status changes.
 */
interface SpaceInviteLinkStore {

    /**
     * Observable state of all space invite links
     */
    val state: StateFlow<Map<SpaceId, SpaceInviteLinkAccessLevel>>

    /**
     * Updates the invite link access level for a specific space
     * @param spaceId The space to update
     * @param accessLevel The new access level for the space
     */
    fun update(spaceId: SpaceId, accessLevel: SpaceInviteLinkAccessLevel)

    /**
     * Observes the invite link access level for a specific space
     * @param spaceId The space to observe
     * @return Flow emitting the current access level for the space
     */
    fun observe(spaceId: SpaceId): Flow<SpaceInviteLinkAccessLevel>
}

class SpaceInviteLinkStoreImpl : SpaceInviteLinkStore {

    private val inviteLinkStates =
        MutableStateFlow<Map<SpaceId, SpaceInviteLinkAccessLevel>>(emptyMap())

    override val state: StateFlow<Map<SpaceId, SpaceInviteLinkAccessLevel>> =
        inviteLinkStates.asStateFlow()

    override fun update(spaceId: SpaceId, accessLevel: SpaceInviteLinkAccessLevel) {
        inviteLinkStates.value = inviteLinkStates.value.toMutableMap().apply {
            put(spaceId, accessLevel)
        }
    }

    override fun observe(spaceId: SpaceId): Flow<SpaceInviteLinkAccessLevel> {
        return inviteLinkStates
            .map { states -> states[spaceId] }
            .filterNotNull()
    }
}