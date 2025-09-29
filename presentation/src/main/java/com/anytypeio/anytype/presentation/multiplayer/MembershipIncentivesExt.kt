package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.domain.`object`.activeReaders
import com.anytypeio.anytype.domain.`object`.activeWriters
import com.anytypeio.anytype.domain.`object`.isSubscriberLimitReached
import timber.log.Timber

fun ObjectWrapper.SpaceView.getIncentiveState(
    isCurrentUserOwner: Boolean, spaceMembers: List<ObjectWrapper.SpaceMember>
): ShareSpaceViewModel.ShareSpaceIncentiveState {
    Timber.d("isCurrentUserOwner: $isCurrentUserOwner, spaceMembers: $spaceMembers")

    if (!shouldShowIncentiveState(
            isCurrentUserOwner = isCurrentUserOwner,
            spaceMembers = spaceMembers
        )
    ) {
        return ShareSpaceViewModel.ShareSpaceIncentiveState.Hidden
    }

    return when {
        isSubscriberLimitReached(
            currentSubscribers = activeReaders(spaceMembers),
            subscriberLimit = readersLimit?.toInt()
        ) -> ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceReaders

        isSubscriberLimitReached(
            currentSubscribers = activeWriters(spaceMembers),
            subscriberLimit = writersLimit?.toInt()
        ) -> ShareSpaceViewModel.ShareSpaceIncentiveState.VisibleSpaceEditors

        else -> ShareSpaceViewModel.ShareSpaceIncentiveState.Hidden
    }
}

private fun ObjectWrapper.SpaceView.shouldShowIncentiveState(
    isCurrentUserOwner: Boolean, spaceMembers: List<ObjectWrapper.SpaceMember>
): Boolean {
    return isCurrentUserOwner &&
            spaceAccessType == SpaceAccessType.SHARED &&
            spaceMembers.any {
                it.status == ParticipantStatus.JOINING
                        || (it.status == ParticipantStatus.ACTIVE && it.permissions == SpaceMemberPermissions.READER)
            }
}