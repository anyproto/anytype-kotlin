package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel.VmParams
import javax.inject.Inject

class LinkToObjectViewModelFactory(
    private val vmParams: VmParams,
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LinkToObjectViewModel(
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceViews = spaceViews
        ) as T
    }
}

class LinkToObjectOrWebViewModelFactory(
    private val vmParams: LinkToObjectOrWebViewModel.VmParams,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val stores: Editor.Storage,
    private val urlValidator: UrlValidator,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LinkToObjectOrWebViewModel(
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            stores = stores,
            urlValidator = urlValidator,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            fieldParser = fieldParser,
            spaceViews = spaceViews
        ) as T
    }
}

class BackLinkOrAddToObjectViewModelFactory @Inject constructor(
    private val vmParams: VmParams,
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BackLinkOrAddToObjectViewModel(
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceViews = spaceViews
        ) as T
    }
}