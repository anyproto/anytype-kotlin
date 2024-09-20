package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.VmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import javax.inject.Inject

class AllContentViewModelFactory @Inject constructor(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storeOfRelations: StoreOfRelations,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AllContentViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            urlBuilder = urlBuilder,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        ) as T
}