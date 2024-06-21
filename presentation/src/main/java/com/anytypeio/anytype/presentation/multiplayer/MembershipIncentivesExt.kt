package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.`object`.canAddReaders
import com.anytypeio.anytype.domain.`object`.canAddWriters

fun ObjectWrapper.SpaceView.getIncentiveState(
    isCurrentUserOwner: Boolean, spaceMembers: List<ObjectWrapper.SpaceMember>
): ShareSpaceViewModel.ShareSpaceIncentiveState {
    val canAddReaders = canAddReaders(isCurrentUserOwner, spaceMembers)
    val canAddWriters = canAddWriters(isCurrentUserOwner, spaceMembers)
    return when {
        !canAddReaders -> ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceReaders
        !canAddWriters -> ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceEditors
        else -> ShareSpaceViewModel.ShareSpaceIncentiveState.Hidden
    }
}