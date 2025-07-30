package com.anytypeio.anytype.feature_allcontent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.VmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import javax.inject.Inject

class AllContentViewModelFactory @Inject constructor(
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val urlBuilder: UrlBuilder,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val updateAllContentState: UpdateAllContentState,
    private val restoreAllContentState: RestoreAllContentState,
    private val searchObjects: SearchObjects,
    private val localeProvider: LocaleProvider,
    private val createObject: CreateObject,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val removeObjectsFromWorkspace: RemoveObjectsFromWorkspace,
    private val userPermissionProvider: UserPermissionProvider,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AllContentViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            restoreAllContentState = restoreAllContentState,
            updateAllContentState = updateAllContentState,
            searchObjects = searchObjects,
            localeProvider = localeProvider,
            createObject = createObject,
            setObjectListIsArchived = setObjectListIsArchived,
            removeObjectsFromWorkspace = removeObjectsFromWorkspace,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser,
            spaceViews = spaceViews
        ) as T
}