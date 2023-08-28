package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectSetViewModelFactory(
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val setObjectDetails: UpdateDetail,
    private val createDataViewObject: CreateDataViewObject,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val urlBuilder: UrlBuilder,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val createObject: CreateObject,
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
    private val setQueryToObjectSet: SetQueryToObjectSet,
    private val database: ObjectSetDatabase,
    private val paginator: ObjectSetPaginator,
    private val storeOfRelations: StoreOfRelations,
    private val objectStateReducer: ObjectStateReducer,
    private val dataViewSubscription: DataViewSubscription,
    private val workspaceManager: WorkspaceManager,
    private val objectStore: ObjectStore,
    private val addObjectToCollection: AddObjectToCollection,
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getDefaultPageType: GetDefaultPageType,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val duplicateObjects: DuplicateObjects,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val viewerDelegate: ViewerDelegate
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ObjectSetViewModel(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            setObjectDetails = setObjectDetails,
            createDataViewObject = createDataViewObject,
            setDocCoverImage = setDocCoverImage,
            downloadUnsplashImage = downloadUnsplashImage,
            updateText = updateText,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            dispatcher = dispatcher,
            delegator = delegator,
            coverImageHashProvider = coverImageHashProvider,
            urlBuilder = urlBuilder,
            session = session,
            analytics = analytics,
            createObject = createObject,
            dataViewSubscriptionContainer = dataViewSubscriptionContainer,
            cancelSearchSubscription = cancelSearchSubscription,
            setQueryToObjectSet = setQueryToObjectSet,
            database = database,
            paginator = paginator,
            storeOfRelations = storeOfRelations,
            stateReducer = objectStateReducer,
            dataViewSubscription = dataViewSubscription,
            workspaceManager = workspaceManager,
            objectStore = objectStore,
            addObjectToCollection = addObjectToCollection,
            objectToCollection = objectToCollection,
            storeOfObjectTypes = storeOfObjectTypes,
            getDefaultPageType = getDefaultPageType,
            updateDataViewViewer = updateDataViewViewer,
            duplicateObjects = duplicateObjects,
            templatesContainer = templatesContainer,
            setObjectListIsArchived = setObjectListIsArchived,
            viewerDelegate = viewerDelegate
        ) as T
    }
}