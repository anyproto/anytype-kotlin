package com.anytypeio.anytype.presentation.multiplayer

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.domain.`object`.activeReaders
import com.anytypeio.anytype.domain.`object`.activeWriters
import com.anytypeio.anytype.domain.`object`.isSubscriberLimitReached
import timber.log.Timber

fun ObjectWrapper.SpaceView.spaceLimitsState(
    isCurrentUserOwner: Boolean,
    spaceMembers: List<ObjectWrapper.SpaceMember>,
    sharedSpaceCount: Int,
    sharedSpaceLimit: Int,
): SpaceLimitsState {
    Timber.d("isCurrentUserOwner: $isCurrentUserOwner, spaceMembers: $spaceMembers")

    if (sharedSpaceLimit > 0
        && sharedSpaceCount >= sharedSpaceLimit
        && spaceAccessType != SpaceAccessType.SHARED
    ) {
        return SpaceLimitsState.SharableLimit(
            count = sharedSpaceLimit
        )
    }

    if (!shouldShowIncentiveState(
            isCurrentUserOwner = isCurrentUserOwner,
            spaceMembers = spaceMembers
        )
    ) {
        return SpaceLimitsState.Init
    }

    return when {
        isSubscriberLimitReached(
            currentSubscribers = activeReaders(spaceMembers),
            subscriberLimit = readersLimit?.toInt()
        ) -> SpaceLimitsState.ViewersLimit(
            count = readersLimit?.toInt() ?: 0
        )

        isSubscriberLimitReached(
            currentSubscribers = activeWriters(spaceMembers),
            subscriberLimit = writersLimit?.toInt()
        ) -> SpaceLimitsState.EditorsLimit(
            count = writersLimit?.toInt() ?: 0
        )

        else -> SpaceLimitsState.Init
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