package com.anytypeio.anytype.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.SetObjectType
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.networkmode.GetNetworkMode
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.ConvertObjectToSet
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.CreateObjectAsMentionOrLink
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.templates.ApplyTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableDelegate
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.UrlHelper
import javax.inject.Inject

open class EditorViewModelFactory @Inject constructor(
    private val params: EditorViewModel.Params,
    private val permissions: UserPermissionProvider,
    private val openPage: OpenPage,
    private val closeObject: CloseObject,
    private val createObjectSet: CreateObjectSet,
    private val createBlockLinkWithObject: CreateBlockLinkWithObject,
    private val createObjectAsMentionOrLink: CreateObjectAsMentionOrLink,
    private val interceptEvents: InterceptEvents,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val documentEventReducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val updateDetail: UpdateDetail,
    private val searchObjects: SearchObjects,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val findObjectSetForType: FindObjectSetForType,
    private val copyFileToCacheDirectory: CopyFileToCacheDirectory,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val setDocImageIcon: SetDocumentImageIcon,
    private val createObject: CreateObject,
    private val objectToSet: ConvertObjectToSet,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val featureToggles: FeatureToggles,
    private val tableDelegate: EditorTableDelegate,
    private val spaceManager: SpaceManager,
    private val getObjectTypes: GetObjectTypes,
    private val objectToCollection: ConvertObjectToCollection,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val addRelationToObject: AddRelationToObject,
    private val applyTemplate: ApplyTemplate,
    private val setObjectType: SetObjectType,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers,
    private val getNetworkMode: GetNetworkMode,
    private val clearLastOpenedObject: ClearLastOpenedObject,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val syncStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val fieldParser : FieldParser,
    private val dateProvider: DateProvider,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlHelper: UrlHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(
            vmParams = params,
            permissions = permissions,
            openPage = openPage,
            closePage = closeObject,
            createBlockLinkWithObject = createBlockLinkWithObject,
            createObjectAsMentionOrLink = createObjectAsMentionOrLink,
            interceptEvents = interceptEvents,
            updateLinkMarks = updateLinkMarks,
            removeLinkMark = removeLinkMark,
            reducer = documentEventReducer,
            urlBuilder = urlBuilder,
            renderer = renderer,
            orchestrator = orchestrator,
            analytics = analytics,
            dispatcher = dispatcher,
            delegator = delegator,
            updateDetail = updateDetail,
            searchObjects = searchObjects,
            getDefaultObjectType = getDefaultObjectType,
            findObjectSetForType = findObjectSetForType,
            createObjectSet = createObjectSet,
            copyFileToCache = copyFileToCacheDirectory,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            setDocImageIcon = setDocImageIcon,
            createObject = createObject,
            objectToSet = objectToSet,
            objectToCollection = objectToCollection,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            featureToggles = featureToggles,
            tableDelegate = tableDelegate,
            spaceManager = spaceManager,
            getObjectTypes = getObjectTypes,
            interceptFileLimitEvents = interceptFileLimitEvents,
            addRelationToObject = addRelationToObject,
            applyTemplate = applyTemplate,
            setObjectType = setObjectType,
            templatesContainer = templatesContainer,
            dispatchers = dispatchers,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceSyncAndP2PStatusProvider = syncStatusProvider,
            getNetworkMode = getNetworkMode,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            clearLastOpenedObject = clearLastOpenedObject,
            fieldParser = fieldParser,
            dateProvider = dateProvider,
            spaceViews = spaceViews,
            urlHelper = urlHelper
        ) as T
    }
}