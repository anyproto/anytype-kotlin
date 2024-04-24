package com.anytypeio.anytype.presentation.analytics

import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import javax.inject.Inject

interface AnalyticSpaceHelperDelegate {

    fun provideParams(spaceId: SpaceId): Params

    data class Params(
        val permission: String,
        val spaceType: String
    ) {
        companion object {
            val EMPTY = Params("", "")
        }
    }
}

class DefaultAnalyticsParamsProvider @Inject constructor(
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : AnalyticSpaceHelperDelegate {

    override fun provideParams(spaceId: SpaceId): AnalyticSpaceHelperDelegate.Params {
        val permissions = userPermissionProvider.get(spaceId)
        val spaceType = spaceViewContainer.get(spaceId)?.spaceAccessType
        return AnalyticSpaceHelperDelegate.Params(
            permission = when (permissions) {
                SpaceMemberPermissions.READER -> "Reader"
                SpaceMemberPermissions.WRITER -> "Writer"
                SpaceMemberPermissions.OWNER -> "Owner"
                SpaceMemberPermissions.NO_PERMISSIONS -> "NoPermissions"
                null -> ""
            },
            spaceType = when (spaceType) {
                SpaceAccessType.DEFAULT -> "Personal"
                SpaceAccessType.PRIVATE -> "Private"
                SpaceAccessType.SHARED -> "Shared"
                else -> ""
            }
        )
    }
}

