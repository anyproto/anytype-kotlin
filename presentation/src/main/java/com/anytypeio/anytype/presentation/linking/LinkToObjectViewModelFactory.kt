package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.Editor
import javax.inject.Inject

class LinkToObjectViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LinkToObjectViewModel(
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            spaceManager = spaceManager,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        ) as T
    }
}

class LinkToObjectOrWebViewModelFactory(
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val stores: Editor.Storage,
    private val urlValidator: UrlValidator,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LinkToObjectOrWebViewModel(
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            stores = stores,
            urlValidator = urlValidator,
            spaceManager = spaceManager,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        ) as T
    }
}

class BackLinkOrAddToObjectViewModelFactory @Inject constructor(
    private val urlBuilder: UrlBuilder,
    private val getObjectTypes: GetObjectTypes,
    private val searchObjects: SearchObjects,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BackLinkOrAddToObjectViewModel(
            urlBuilder = urlBuilder,
            getObjectTypes = getObjectTypes,
            searchObjects = searchObjects,
            analytics = analytics,
            spaceManager = spaceManager,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
        ) as T
    }
}