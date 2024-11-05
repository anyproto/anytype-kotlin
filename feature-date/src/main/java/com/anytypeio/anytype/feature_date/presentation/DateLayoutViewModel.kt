package com.anytypeio.anytype.feature_date.presentation

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate

class DateLayoutViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    data class VmParams(
        val spaceId: SpaceId
    )
}