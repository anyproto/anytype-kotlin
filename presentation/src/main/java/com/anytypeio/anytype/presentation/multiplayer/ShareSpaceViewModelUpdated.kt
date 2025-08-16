package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.getOrThrow
import com.anytypeio.anytype.domain.multiplayer.UpdateSpaceInviteLinkAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Extension methods for ShareSpaceViewModel to support new invite link access levels
 * These should be integrated into the main ShareSpaceViewModel
 */

data class InviteLinkAccessState(
    val currentLevel: SpaceInviteLinkAccessLevel = SpaceInviteLinkAccessLevel.LINK_DISABLED,
    val activeLink: SpaceInviteLink? = null,
    val isLoading: Boolean = false,
    val showConfirmationFor: SpaceInviteLinkAccessLevel? = null
)

/**
 * Methods to add to ShareSpaceViewModel
 */
interface ShareSpaceViewModelExtensions {
    
    val inviteLinkAccessState: StateFlow<InviteLinkAccessState>
    
    /**
     * Called when user selects a new invite link access level
     */
    fun onInviteLinkAccessLevelSelected(newLevel: SpaceInviteLinkAccessLevel)
    
    /**
     * Called when user confirms changing the invite link access level
     */
    fun onInviteLinkAccessChangeConfirmed(newLevel: SpaceInviteLinkAccessLevel)
    
    /**
     * Called when user cancels the confirmation dialog
     */
    fun onInviteLinkAccessChangeCancel()
    
    /**
     * Updates the current invite link access state based on space state
     */
    fun updateInviteLinkAccessState(space: SpaceId, link: SpaceInviteLink?)
}

/**
 * Implementation helper for the new invite link access functionality
 * This should be integrated into ShareSpaceViewModel
 */
class InviteLinkAccessManager(
    private val updateSpaceInviteLinkAccess: UpdateSpaceInviteLinkAccess,
    private val space: SpaceId
) {
    
    private val _inviteLinkAccessState = MutableStateFlow(InviteLinkAccessState())
    val inviteLinkAccessState: StateFlow<InviteLinkAccessState> = _inviteLinkAccessState
    
    fun onAccessLevelSelected(newLevel: SpaceInviteLinkAccessLevel, scope: kotlinx.coroutines.CoroutineScope) {
        val currentLevel = _inviteLinkAccessState.value.currentLevel
        
        // Check if confirmation is needed
        if (currentLevel.needsConfirmationToChangeTo(newLevel)) {
            _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(
                showConfirmationFor = newLevel
            )
        } else {
            updateAccessLevel(newLevel, scope)
        }
    }
    
    fun onAccessChangeConfirmed(newLevel: SpaceInviteLinkAccessLevel, scope: kotlinx.coroutines.CoroutineScope) {
        _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(
            showConfirmationFor = null
        )
        updateAccessLevel(newLevel, scope)
    }
    
    fun onAccessChangeCancel() {
        _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(
            showConfirmationFor = null
        )
    }
    
    private fun updateAccessLevel(newLevel: SpaceInviteLinkAccessLevel, scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            try {
                _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(isLoading = true)
                
                val result = updateSpaceInviteLinkAccess.async(
                    UpdateSpaceInviteLinkAccess.Params(
                        space = space,
                        currentLevel = _inviteLinkAccessState.value.currentLevel,
                        newLevel = newLevel
                    )
                ).getOrThrow()
                
                _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(
                    currentLevel = newLevel,
                    activeLink = result,
                    isLoading = false
                )
                
                Timber.d("Successfully updated invite link access level to: $newLevel")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to update invite link access level")
                _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(isLoading = false)
                // Error should be handled by the main view model
                throw e
            }
        }
    }
    
    fun updateCurrentState(link: SpaceInviteLink?, accessType: com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType?) {
        val currentLevel = when {
            link == null -> SpaceInviteLinkAccessLevel.LINK_DISABLED
            // TODO: Determine actual level from invite type/permissions
            // For now, default to EDITOR_ACCESS if link exists
            else -> SpaceInviteLinkAccessLevel.EDITOR_ACCESS
        }
        
        _inviteLinkAccessState.value = _inviteLinkAccessState.value.copy(
            currentLevel = currentLevel,
            activeLink = link
        )
    }
}