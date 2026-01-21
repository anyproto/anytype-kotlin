package com.anytypeio.anytype.presentation.analytics

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.EMPTY_STRING_VALUE
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import javax.inject.Inject

interface AnalyticSpaceHelperDelegate {

    fun provideParams(space: Id): Params

    data class Params(
        val permission: String,
        val spaceType: String,
        val spaceUxType: String,
        val spaceId: String
    ) {
        companion object {
            val EMPTY = Params("", "", "", "")
        }
    }
}

class DefaultAnalyticsParamsProvider @Inject constructor(
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : AnalyticSpaceHelperDelegate {

    override fun provideParams(space: Id): AnalyticSpaceHelperDelegate.Params {
        val spaceId = SpaceId(space)
        val permissions = userPermissionProvider.get(spaceId)
        val spaceView = spaceViewContainer.get(spaceId)
        val spaceType = spaceView?.spaceAccessType
        val spaceUxType = spaceView?.spaceUxType ?: SpaceUxType.DATA
        return AnalyticSpaceHelperDelegate.Params(
            permission = when (permissions) {
                SpaceMemberPermissions.READER -> "Reader"
                SpaceMemberPermissions.WRITER -> "Writer"
                SpaceMemberPermissions.OWNER -> "Owner"
                SpaceMemberPermissions.NO_PERMISSIONS -> "NoPermissions"
                null -> EMPTY_STRING_VALUE
            },
            spaceType = when (spaceType) {
                SpaceAccessType.DEFAULT -> "Personal"
                SpaceAccessType.PRIVATE -> "Private"
                SpaceAccessType.SHARED -> "Shared"
                else -> EMPTY_STRING_VALUE
            },
            spaceUxType = when (spaceUxType) {
                SpaceUxType.DATA -> "Data"
                SpaceUxType.STREAM -> "Stream"
                SpaceUxType.CHAT -> "Chat"
                SpaceUxType.ONE_TO_ONE -> "OneToOne"
                SpaceUxType.NONE -> EMPTY_STRING_VALUE
            },
            spaceId = space
        )
    }
}

