package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.relations.SpacePropertiesViewModel.VmParams
import javax.inject.Inject

class SpacePropertiesViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val userPermissionProvider: UserPermissionProvider,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    data class VmParams(
        val spaceId: Id
    )
}

class SpacePropertiesVmFactory @Inject constructor(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val userPermissionProvider: UserPermissionProvider,
    private val fieldParser: FieldParser
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpacePropertiesViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser
        ) as T
}
