package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate

class MoveToViewModelFactory(
    private val vmParams: MoveToViewModel.VmParams,
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MoveToViewModel(
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        ) as T
    }
}