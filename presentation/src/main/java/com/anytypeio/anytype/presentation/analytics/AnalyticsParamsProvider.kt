package com.anytypeio.anytype.presentation.analytics

import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider

interface AnalyticsParamsProvider {

    fun provideParams(spaceId: SpaceId): Params

    data class Params(
        val permission: String,
        val spaceType: String
    )
}

class DefaultAnalyticsParamsProvider constructor(
    private val userPermissionProvider: UserPermissionProvider,
    private val spaceViewContainer: SpaceViewSubscriptionContainer
) : AnalyticsParamsProvider {

    override fun provideParams(spaceId: SpaceId): AnalyticsParamsProvider.Params {
        return proceedWithObservingSpaceView(spaceId)
    }

    private fun proceedWithObservingSpaceView(spaceId: SpaceId): AnalyticsParamsProvider.Params {
        val permissions = userPermissionProvider.get(spaceId)
        val spaceType = spaceViewContainer.get(spaceId)?.spaceAccessType
        return AnalyticsParamsProvider.Params(
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

